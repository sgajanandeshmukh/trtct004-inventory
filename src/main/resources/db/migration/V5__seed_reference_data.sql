-- V5__seed_reference_data.sql
-- Seeds reference data: categories, warehouses, suppliers.
-- REG-IDs and HV-IDs externalized as values.

-- -----------------------------------------------------------------------------
-- Category Master  DB-002 (CTGMST)
-- -----------------------------------------------------------------------------

INSERT INTO inventory.ctgmst (cgctid, cgctnm, cgctds, cgstat) VALUES
    ('ELECT', 'Electronics',     'Electronic components and devices', 'A'),
    ('MECH',  'Mechanical',      'Mechanical parts and assemblies',   'A'),
    ('CHEM',  'Chemicals',       'Chemical products and compounds',   'A'),
    ('PACK',  'Packaging',       'Packaging materials and containers','A'),
    ('TOOL',  'Tools',           'Hand and power tools',              'A'),
    ('SAFE',  'Safety Equipment','Personal protective equipment',     'A');

-- -----------------------------------------------------------------------------
-- Warehouse Master  DB-004 (WHSMST)
-- -----------------------------------------------------------------------------

INSERT INTO inventory.whsmst (whwhid, whwhnm, whloca, whstat) VALUES
    ('WH01', 'Main Warehouse',    '100 Industrial Blvd, Site A', 'A'),
    ('WH02', 'Secondary Storage', '200 Logistics Ave, Site B',  'A'),
    ('WH03', 'Distribution Hub',  '300 Freight Rd, Site C',     'A');

-- -----------------------------------------------------------------------------
-- Supplier Master  DB-003 (SUPLMST)
-- SYNTHETIC PII: Contact names/phones/emails are synthetic non-real values
-- -----------------------------------------------------------------------------

INSERT INTO inventory.suplmst (spspid, spspnm, spctct, spphon, spemal, spstat) VALUES
    ('SUP001', 'Acme Components Inc.',    'A. Contact-1', '555-0101', 'contact1@synthetic.test', 'A'),
    ('SUP002', 'Global Parts Ltd.',       'B. Contact-2', '555-0102', 'contact2@synthetic.test', 'A'),
    ('SUP003', 'Eastern Manufacturing',   'C. Contact-3', '555-0103', 'contact3@synthetic.test', 'A');
