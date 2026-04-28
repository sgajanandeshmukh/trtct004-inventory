-- V3__create_indexes.sql
-- Performance indexes on FK fields and frequently queried columns.
-- Source: DOC-02 Section 3  Relationship Map

-- -----------------------------------------------------------------------------
-- Inventory domain indexes
-- -----------------------------------------------------------------------------

-- ITMMST: FK  CTGMST (BR-012, BR-039, BR-061)
CREATE INDEX idx_itmmst_ctgy ON inventory.itmmst (imctgy);

-- ITMMST: FK  SUPLMST (BR-041, BR-060)
CREATE INDEX idx_itmmst_supl ON inventory.itmmst (imsupl);

-- ITMMST: FK  WHSMST (BR-043, BR-060)
CREATE INDEX idx_itmmst_whse ON inventory.itmmst (imwhse);

-- ITMMST: Status filter (BR-059  active items only for reorder report)
CREATE INDEX idx_itmmst_stat ON inventory.itmmst (imstat);

-- ITMMST: Reorder report filter (BR-062)
CREATE INDEX idx_itmmst_reorder ON inventory.itmmst (imstat, imropl, imqtoh)
    WHERE imropl > 0;

-- INVTRN: FK  ITMMST (audit trail queries)
CREATE INDEX idx_invtrn_itmid ON inventory.invtrn (tritmid);

-- INVTRN: Date-based queries
CREATE INDEX idx_invtrn_date ON inventory.invtrn (trdate);
