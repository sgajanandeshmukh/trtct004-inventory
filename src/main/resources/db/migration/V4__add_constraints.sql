-- V4__add_constraints.sql
-- Foreign key constraints for inventory domain inferred relationships.
-- IMPORTANT: All relationships below are INFERRED from field naming conventions,
-- data usage patterns in DDS source, and program behavior analysis.
-- No explicit FK constraints existed in the legacy RPG/DDS environment.
-- Source: DOC-02 Section 3  Data Relationship Map

-- -----------------------------------------------------------------------------
-- Inventory domain FK constraints (DOC-02 Section 3.2)
-- -----------------------------------------------------------------------------

-- INFERRED: ITMMST.imctgy  CTGMST.cgctid (One-to-Many  category classifies many items)
ALTER TABLE inventory.itmmst
    ADD CONSTRAINT fk_itmmst_ctgy
    FOREIGN KEY (imctgy) REFERENCES inventory.ctgmst (cgctid);

-- INFERRED: ITMMST.imsupl  SUPLMST.spspid (One-to-Many  supplier supplies many items)
ALTER TABLE inventory.itmmst
    ADD CONSTRAINT fk_itmmst_supl
    FOREIGN KEY (imsupl) REFERENCES inventory.suplmst (spspid);

-- INFERRED: ITMMST.imwhse  WHSMST.whwhid (One-to-Many  warehouse stocks many items)
ALTER TABLE inventory.itmmst
    ADD CONSTRAINT fk_itmmst_whse
    FOREIGN KEY (imwhse) REFERENCES inventory.whsmst (whwhid);

-- INFERRED: INVTRN.tritmid  ITMMST.imitid (One-to-Many  item has many transactions)
ALTER TABLE inventory.invtrn
    ADD CONSTRAINT fk_invtrn_itm
    FOREIGN KEY (tritmid) REFERENCES inventory.itmmst (imitid);
