-- Seed data for TRTCT004 Inventory Management (H2 in-memory)
-- Table: inventory.itmmst  (schema created via H2 INIT parameter in application.yml)
-- Columns: imitid, imitnm, imitds, imctgy, imqtoh, imqtal, imqtor, imropl, improq, imuncs, imunpr, imsupl, imwhse, imstat, imlupd

INSERT INTO inventory.itmmst (imitid, imitnm, imitds, imctgy, imqtoh, imqtal, imqtor, imropl, improq, imuncs, imunpr, imsupl, imwhse, imstat, imlupd) VALUES
-- TC01 target: margin = (15-10)/15 * 100 = 33.33%
('ITEM0001', 'Widget Alpha',      'Standard widget unit A',      'ELECT', 100,  0, 25, 50, 60, 10.00, 15.00, 'SUP001', 'WH01', 'ACTIVE',   '2025-01-15'),
-- TC04/below reorder: qty=15, rp=50 → below reorder point
('ITEM0002', 'Sprocket Beta',     'Precision sprocket B',        'MECH',   15,  0,  0, 50, 30, 20.00, 30.00, 'SUP002', 'WH01', 'ACTIVE',   '2025-02-10'),
-- TC04 boundary: qty=50, rp=50 → AT reorder point (qualifies)
('ITEM0003', 'Solvent Gamma',     'Industrial solvent G',        'CHEM',   50,  0, 10, 50, 25,  8.00, 12.50, 'SUP003', 'WH02', 'ACTIVE',   '2025-03-05'),
-- Above reorder: qty=200, rp=100 → does NOT appear in reorder report
('ITEM0004', 'Pallet Delta',      'Standard shipping pallet',    'PACK',  200,  0,  0,100, 50,  5.00,  7.50, 'SUP004', 'WH02', 'ACTIVE',   '2025-01-20'),
-- TC03 target: allocated=10 → delete BLOCKED (BR-017/019)
('ITEM0005', 'Relay Epsilon',     'High-voltage relay unit',     'ELECT',  80, 10,  5, 30, 20, 45.00, 70.00, 'SUP001', 'WH03', 'ACTIVE',   '2025-02-28'),
-- INACTIVE item: should not appear in reorder report
('ITEM0006', 'Obsolete Zeta',     'Discontinued part Z',         'MECH',    5,  0,  0,  0,  0,  3.00,  5.00, 'SUP002', 'WH01', 'INACTIVE', '2024-12-01'),
-- Below reorder: qty=10, rp=100
('ITEM0007', 'Cable Eta',         'Power cable type H',          'ELECT',  10,  0,  0,100, 50,  2.50,  4.00, 'SUP001', 'WH01', 'ACTIVE',   '2025-03-10'),
-- Above reorder: qty=500, rp=200
('ITEM0008', 'Bolt Theta',        'Grade-8 hex bolt set',        'MECH',  500,  0,  0,200,100,  0.50,  0.90, 'SUP002', 'WH02', 'ACTIVE',   '2025-01-05'),
-- Below reorder: qty=5, rp=20
('ITEM0009', 'Filter Iota',       'Air filter unit I',           'MISC',    5,  0,  0, 20, 15, 12.00, 18.00, 'SUP005', 'WH04', 'ACTIVE',   '2025-02-15'),
-- No reorder point defined (rp=0): excluded from reorder report
('ITEM0010', 'Bracket Kappa',     'Universal mounting bracket',  'MECH',   30,  0,  0,  0,  0,  6.00,  9.00, 'SUP002', 'WH01', 'ACTIVE',   '2025-03-01'),
-- Below reorder: qty=8, rp=50
('ITEM0011', 'Capacitor Lambda',  'Electrolytic capacitor 100uF','ELECT',   8,  0,  0, 50, 40,  1.20,  2.00, 'SUP001', 'WH01', 'ACTIVE',   '2025-03-20');
