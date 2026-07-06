INSERT INTO dropshipping_order (order_code, provider_id, product_code, product_description,
                               quantity, street, city, state, postal_code, country,
                               customer_name, customer_contact, expected_delivery_date,
                               special_conditions, status, created_at, updated_at)
VALUES ('ORD-2026-TEST-01', 42, 'PROD-XR500', 'Zapatillas Running XR-500',
        3, 'Av. Naciones Unidas E35-171', 'Quito', 'Pichincha', '170503', 'Ecuador',
        'María García López', 'mgarcia@email.com / +593-99-1234567', '2026-07-15',
        'Entregar solo en horario de oficina (8h-17h)', 'PENDING',
        '2026-07-05 08:00:00', '2026-07-05 08:00:00'),

       ('ORD-2026-TEST-02', 42, 'PROD-MB45L', 'Mochila Outdoor 45L',
        1, 'Calle Reina Victoria N26-64', 'Quito', 'Pichincha', '170517', 'Ecuador',
        'Carlos Mendoza', 'cmendoza@empresa.com / +593-98-9876543', '2026-07-18',
        NULL, 'PENDING',
        '2026-07-05 09:00:00', '2026-07-05 09:00:00'),

       ('ORD-2026-TEST-03', 42, 'PROD-GPS100', 'Reloj Deportivo GPS',
        2, 'Av. Eloy Alfaro N36-168', 'Quito', 'Pichincha', '170505', 'Ecuador',
        'Ana Torres', 'atorres@cliente.com / +593-97-1112233', '2026-07-20',
        'Requiere empaque especial antihumedad', 'ACCEPTED',
        '2026-07-04 10:00:00', '2026-07-05 11:00:00');

INSERT INTO order_status_event (order_id, previous_status, new_status, actor_id,
                                timestamp, estimated_dispatch_date, rejection_reason)
SELECT id, 'PENDING', 'ACCEPTED', '42',
       '2026-07-05 11:00:00', '2026-07-08', NULL
FROM dropshipping_order
WHERE order_code = 'ORD-2026-TEST-03';
