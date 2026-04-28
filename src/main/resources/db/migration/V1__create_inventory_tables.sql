-- V1__create_inventory_tables.sql
-- Creates all inventory domain tables: ITMMST, CTGMST, SUPLMST, WHSMST, INVTRN
-- Source: DOC-02 Data Architecture Report  DB-001 through DB-005
-- Note: All foreign key relationships are INFERRED (no explicit DDL constraints in legacy DDS system).
--       PII fields: SUPLMST.SPCTCT (NAME, HIGH), SUPLMST.SPPHON (CONTACT), SUPLMST.SPEMAL (CONTACT)

-- -----------------------------------------------------------------------------
-- Schema
-- -----------------------------------------------------------------------------

CREATE SCHEMA IF NOT EXISTS inventory;

-- -----------------------------------------------------------------------------
-- DB-002: Category Master (CTGMST / CTGMSTP)  Reference Data
-- -----------------------------------------------------------------------------

CREATE TABLE inventory.ctgmst (
    cgctid  CHAR(6)     NOT NULL,                           -- FLD-DB002-001: Category ID (PK)
    cgctnm  VARCHAR(25) NOT NULL,                           -- FLD-DB002-002: Category Name
    cgctds  VARCHAR(40),                                    -- FLD-DB002-003: Category Description
    cgstat  CHAR(1)     NOT NULL DEFAULT 'A',               -- FLD-DB002-004: Status (A/I)
    CONSTRAINT pk_ctgmst PRIMARY KEY (cgctid),
    CONSTRAINT ck_ctgmst_stat CHECK (cgstat IN ('A', 'I'))
);

COMMENT ON TABLE inventory.ctgmst IS 'DB-002: Category Master  product category reference data';
COMMENT ON COLUMN inventory.ctgmst.cgstat IS 'Status: A=Active, I=Inactive';

-- -----------------------------------------------------------------------------
-- DB-003: Supplier Master (SUPLMST / SUPLMSTP)  Reference Data
-- PII: spctct (NAME, HIGH), spphon (CONTACT), spemal (CONTACT)
-- -----------------------------------------------------------------------------

CREATE TABLE inventory.suplmst (
    spspid  CHAR(6)     NOT NULL,                           -- FLD-DB003-001: Supplier ID (PK)
    spspnm  VARCHAR(30) NOT NULL,                           -- FLD-DB003-002: Supplier Name
    spctct  VARCHAR(25),                                    -- FLD-DB003-003: Contact Name [PII: NAME, HIGH]
    spphon  VARCHAR(15),                                    -- FLD-DB003-004: Contact Phone [PII: CONTACT]
    spemal  VARCHAR(40),                                    -- FLD-DB003-005: Contact Email [PII: CONTACT]
    spstat  CHAR(1)     NOT NULL DEFAULT 'A',               -- FLD-DB003-006: Status (A/I)
    CONSTRAINT pk_suplmst PRIMARY KEY (spspid),
    CONSTRAINT ck_suplmst_stat CHECK (spstat IN ('A', 'I'))
);

COMMENT ON TABLE inventory.suplmst IS 'DB-003: Supplier Master  PII: spctct, spphon, spemal require access controls and masking in non-production';
COMMENT ON COLUMN inventory.suplmst.spctct IS '[PII: NAME, HIGH] Contact person name  restrict access to authorized users';
COMMENT ON COLUMN inventory.suplmst.spphon IS '[PII: CONTACT, MEDIUM] Contact phone  mask in logs and reports';
COMMENT ON COLUMN inventory.suplmst.spemal IS '[PII: CONTACT, MEDIUM] Contact email  mask in logs and reports';

-- -----------------------------------------------------------------------------
-- DB-004: Warehouse Master (WHSMST / WHSMSTP)  Reference Data
-- -----------------------------------------------------------------------------

CREATE TABLE inventory.whsmst (
    whwhid  CHAR(4)     NOT NULL,                           -- FLD-DB004-001: Warehouse ID (PK)
    whwhnm  VARCHAR(25) NOT NULL,                           -- FLD-DB004-002: Warehouse Name
    whloca  VARCHAR(30),                                    -- FLD-DB004-003: Location
    whstat  CHAR(1)     NOT NULL DEFAULT 'A',               -- FLD-DB004-004: Status (A/I)
    CONSTRAINT pk_whsmst PRIMARY KEY (whwhid),
    CONSTRAINT ck_whsmst_stat CHECK (whstat IN ('A', 'I'))
);

COMMENT ON TABLE inventory.whsmst IS 'DB-004: Warehouse Master  warehouse location reference data';

-- -----------------------------------------------------------------------------
-- DB-001: Item Master (ITMMST / ITMMSTP)  Owned by DD-L4-001
-- -----------------------------------------------------------------------------

CREATE TABLE inventory.itmmst (
    imitid  CHAR(8)         NOT NULL,                       -- FLD-DB001-001: Item ID (PK)
    imitnm  VARCHAR(30)     NOT NULL,                       -- FLD-DB001-002: Item Name (UNIQUE)
    imitds  VARCHAR(50),                                    -- FLD-DB001-003: Item Description
    imctgy  CHAR(6)         NOT NULL,                       -- FLD-DB001-004: Category Code (FKctgmst)
    imqtoh  INTEGER         NOT NULL DEFAULT 0,             -- FLD-DB001-005: Qty On Hand
    imqtal  INTEGER         NOT NULL DEFAULT 0,             -- FLD-DB001-006: Qty Allocated (HV-023)
    imqtor  INTEGER         NOT NULL DEFAULT 0,             -- FLD-DB001-007: Qty On Order (HV-024)
    imropl  INTEGER,                                        -- FLD-DB001-008: Reorder Point
    improq  INTEGER,                                        -- FLD-DB001-009: Reorder Quantity
    imuncs  NUMERIC(9,2)    NOT NULL,                       -- FLD-DB001-010: Unit Cost [FINANCIAL]
    imunpr  NUMERIC(9,2)    NOT NULL,                       -- FLD-DB001-011: Unit Price [FINANCIAL]
    imsupl  CHAR(6)         NOT NULL,                       -- FLD-DB001-012: Supplier Code (FKsuplmst)
    imwhse  CHAR(4)         NOT NULL,                       -- FLD-DB001-013: Warehouse Code (FKwhsmst)
    imstat  VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',      -- FLD-DB001-014: Status (STM-001)
    imlupd  DATE,                                           -- FLD-DB001-015: Last Updated Date
    CONSTRAINT pk_itmmst PRIMARY KEY (imitid),
    CONSTRAINT uq_itmmst_name UNIQUE (imitnm),
    CONSTRAINT ck_itmmst_stat CHECK (imstat IN ('ACTIVE', 'INACTIVE', 'PENDING_DELETE', 'DELETED')),
    CONSTRAINT ck_itmmst_price CHECK (imunpr >= 0),
    CONSTRAINT ck_itmmst_cost CHECK (imuncs >= 0)
);

COMMENT ON TABLE inventory.itmmst IS 'DB-001: Item Master  central inventory catalog; STM-001 lifecycle managed by InventoryItemEntity';
COMMENT ON COLUMN inventory.itmmst.imstat IS 'STM-001 status: ACTIVE(default), INACTIVE(soft-delete), PENDING_DELETE, DELETED';

-- -----------------------------------------------------------------------------
-- DB-005: Inventory Transaction Log (INVTRN / INVTRNP)
-- Note: No writer in analyzed scope (SME-004). Table preserved for future use.
-- -----------------------------------------------------------------------------

CREATE TABLE inventory.invtrn (
    erseq   BIGSERIAL       NOT NULL,                       -- Surrogate PK (sequential  SME-004)
    trtrid  BIGINT          NOT NULL,                       -- FLD-DB005-001: Transaction ID
    tritmid CHAR(8)         NOT NULL,                       -- FLD-DB005-002: Item ID (FKitmmst)
    trtype  CHAR(2)         NOT NULL,                       -- FLD-DB005-003: Transaction Type
    trqty   INTEGER         NOT NULL,                       -- FLD-DB005-004: Quantity
    trdate  DATE            NOT NULL,                       -- FLD-DB005-005: Transaction Date
    truser  VARCHAR(10),                                    -- FLD-DB005-006: User ID
    trref   VARCHAR(20),                                    -- FLD-DB005-007: Reference
    CONSTRAINT pk_invtrn PRIMARY KEY (erseq)
);

COMMENT ON TABLE inventory.invtrn IS 'DB-005: Inventory Transaction Log  no writer in current scope (SME-004); preserved for future audit trail use';
