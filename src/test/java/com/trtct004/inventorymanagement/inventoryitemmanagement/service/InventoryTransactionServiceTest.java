package com.trtct004.inventorymanagement.inventoryitemmanagement.service;

import com.trtct004.inventorymanagement.inventoryitemmanagement.entity.InventoryTransactionEntity;
import com.trtct004.inventorymanagement.inventoryitemmanagement.repository.InventoryTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceTest {

    @Mock private InventoryTransactionRepository transactionRepository;

    private InventoryTransactionService service;

    @BeforeEach
    void setUp() {
        service = new InventoryTransactionService(transactionRepository);
    }

    @Nested
    @DisplayName("logTransaction — core audit logging")
    class LogTransactionTests {

        @Test
        @DisplayName("creates transaction entity with all fields populated")
        void logTransaction_setsAllFields() {
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryTransactionEntity result = service.logTransaction("ITM00001", "CR", 100, "NEW_ITEM");

            assertThat(result.getItemId()).isEqualTo("ITM00001");
            assertThat(result.getTransactionType()).isEqualTo("CR");
            assertThat(result.getQuantity()).isEqualTo(100);
            assertThat(result.getTransactionDate()).isEqualTo(LocalDate.now());
            assertThat(result.getUserId()).isEqualTo("SYSTEM");
            assertThat(result.getReference()).isEqualTo("NEW_ITEM");
            assertThat(result.getTransactionId()).isNotNull();
        }

        @Test
        @DisplayName("generates unique transaction IDs on sequential calls")
        void logTransaction_uniqueTransactionIds() {
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryTransactionEntity first = service.logTransaction("ITM00001", "CR", 10, "REF1");
            InventoryTransactionEntity second = service.logTransaction("ITM00002", "UP", 20, "REF2");

            assertThat(first.getTransactionId()).isNotEqualTo(second.getTransactionId());
        }

        @Test
        @DisplayName("truncates reference to 20 characters")
        void logTransaction_truncatesLongReference() {
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryTransactionEntity result = service.logTransaction("ITM00001", "SC", 0,
                    "VERY_LONG_REFERENCE_STRING_EXCEEDING_LIMIT");

            assertThat(result.getReference()).hasSize(20);
            assertThat(result.getReference()).isEqualTo("VERY_LONG_REFERENCE_");
        }

        @Test
        @DisplayName("handles null reference without NPE")
        void logTransaction_nullReference() {
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryTransactionEntity result = service.logTransaction("ITM00001", "CR", 0, null);

            assertThat(result.getReference()).isNull();
        }

        @Test
        @DisplayName("reference within limit is preserved as-is")
        void logTransaction_shortReferencePreserved() {
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            InventoryTransactionEntity result = service.logTransaction("ITM00001", "CR", 50, "SHORT");

            assertThat(result.getReference()).isEqualTo("SHORT");
        }

        @Test
        @DisplayName("persists entity via repository.save")
        void logTransaction_callsRepositorySave() {
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.logTransaction("ITM00001", "CR", 100, "NEW_ITEM");

            verify(transactionRepository).save(any(InventoryTransactionEntity.class));
        }
    }

    @Nested
    @DisplayName("logItemCreated — FR-005 item creation audit")
    class LogItemCreatedTests {

        @Test
        @DisplayName("logs CR type with quantity and NEW_ITEM reference")
        void logItemCreated_correctTypeAndReference() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logItemCreated("ITM00001", 50);

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getTransactionType()).isEqualTo("CR");
            assertThat(saved.getQuantity()).isEqualTo(50);
            assertThat(saved.getReference()).isEqualTo("NEW_ITEM");
            assertThat(saved.getItemId()).isEqualTo("ITM00001");
        }
    }

    @Nested
    @DisplayName("logItemUpdated — FR-005 item update audit")
    class LogItemUpdatedTests {

        @Test
        @DisplayName("logs UP type with current quantity")
        void logItemUpdated_correctTypeAndQuantity() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logItemUpdated("ITM00002", 200);

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getTransactionType()).isEqualTo("UP");
            assertThat(saved.getQuantity()).isEqualTo(200);
            assertThat(saved.getReference()).isEqualTo("EDIT");
        }
    }

    @Nested
    @DisplayName("logStatusChange — FR-005 status transition audit")
    class LogStatusChangeTests {

        @Test
        @DisplayName("logs SC type with from->to status reference")
        void logStatusChange_correctReference() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logStatusChange("ITM00001", "ACTIVE", "INACTIVE");

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getTransactionType()).isEqualTo("SC");
            assertThat(saved.getQuantity()).isEqualTo(0);
            assertThat(saved.getReference()).isEqualTo("ACTIVE->INACTIVE");
        }

        @Test
        @DisplayName("truncates long status names in reference")
        void logStatusChange_truncatesLongStatusNames() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logStatusChange("ITM00001", "PENDING_DELETE", "ACTIVE");

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getReference()).isEqualTo("PENDING_DELETE->ACTI");
            assertThat(saved.getReference().length()).isLessThanOrEqualTo(20);
        }
    }

    @Nested
    @DisplayName("logQuantityAdjustment — FR-005 qty change audit")
    class LogQuantityAdjustmentTests {

        @Test
        @DisplayName("logs QA type with delta quantity and adjustment reference")
        void logQuantityAdjustment_correctDelta() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logQuantityAdjustment("ITM00001", 100, 150);

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getTransactionType()).isEqualTo("QA");
            assertThat(saved.getQuantity()).isEqualTo(50);
            assertThat(saved.getReference()).isEqualTo("ADJ:100->150");
        }

        @Test
        @DisplayName("logs negative delta for quantity decrease")
        void logQuantityAdjustment_negativeForDecrease() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logQuantityAdjustment("ITM00001", 200, 50);

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getQuantity()).isEqualTo(-150);
        }

        @Test
        @DisplayName("logs zero delta when quantities are equal")
        void logQuantityAdjustment_zeroDelta() {
            ArgumentCaptor<InventoryTransactionEntity> captor = ArgumentCaptor.forClass(InventoryTransactionEntity.class);
            when(transactionRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

            service.logQuantityAdjustment("ITM00001", 100, 100);

            InventoryTransactionEntity saved = captor.getValue();
            assertThat(saved.getQuantity()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getTransactionHistory — FR-005 item history lookup")
    class GetTransactionHistoryTests {

        @Test
        @DisplayName("delegates to repository with correct itemId")
        void getTransactionHistory_delegatesToRepository() {
            InventoryTransactionEntity txn = InventoryTransactionEntity.builder()
                    .sequenceId(1L).itemId("ITM00001").transactionType("CR").build();
            when(transactionRepository.findByItemIdOrderByTransactionDateDesc("ITM00001"))
                    .thenReturn(List.of(txn));

            List<InventoryTransactionEntity> result = service.getTransactionHistory("ITM00001");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getItemId()).isEqualTo("ITM00001");
        }

        @Test
        @DisplayName("returns empty list when no history exists")
        void getTransactionHistory_emptyList() {
            when(transactionRepository.findByItemIdOrderByTransactionDateDesc("ITM99999"))
                    .thenReturn(List.of());

            List<InventoryTransactionEntity> result = service.getTransactionHistory("ITM99999");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchTransactions — FR-005 filtered search")
    class SearchTransactionsTests {

        @Test
        @DisplayName("passes all filter parameters to repository")
        void searchTransactions_passesFilters() {
            LocalDate start = LocalDate.of(2026, 1, 1);
            LocalDate end = LocalDate.of(2026, 12, 31);
            when(transactionRepository.findByFilters("ITM00001", "CR", start, end))
                    .thenReturn(List.of());

            service.searchTransactions("ITM00001", "CR", start, end);

            verify(transactionRepository).findByFilters("ITM00001", "CR", start, end);
        }

        @Test
        @DisplayName("passes null filters for broad search")
        void searchTransactions_nullFilters() {
            when(transactionRepository.findByFilters(null, null, null, null))
                    .thenReturn(List.of());

            service.searchTransactions(null, null, null, null);

            verify(transactionRepository).findByFilters(null, null, null, null);
        }
    }

    @Nested
    @DisplayName("getRecentTransactions — FR-005 recent activity")
    class GetRecentTransactionsTests {

        @Test
        @DisplayName("limits results to requested count")
        void getRecentTransactions_limitsResults() {
            List<InventoryTransactionEntity> all = List.of(
                    InventoryTransactionEntity.builder().sequenceId(3L).itemId("A").build(),
                    InventoryTransactionEntity.builder().sequenceId(2L).itemId("B").build(),
                    InventoryTransactionEntity.builder().sequenceId(1L).itemId("C").build()
            );
            when(transactionRepository.findRecentTransactions()).thenReturn(all);

            List<InventoryTransactionEntity> result = service.getRecentTransactions(2);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("returns all when limit exceeds available")
        void getRecentTransactions_limitExceedsAvailable() {
            List<InventoryTransactionEntity> all = List.of(
                    InventoryTransactionEntity.builder().sequenceId(1L).itemId("A").build()
            );
            when(transactionRepository.findRecentTransactions()).thenReturn(all);

            List<InventoryTransactionEntity> result = service.getRecentTransactions(100);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty list when no transactions")
        void getRecentTransactions_empty() {
            when(transactionRepository.findRecentTransactions()).thenReturn(List.of());

            List<InventoryTransactionEntity> result = service.getRecentTransactions(20);

            assertThat(result).isEmpty();
        }
    }
}
