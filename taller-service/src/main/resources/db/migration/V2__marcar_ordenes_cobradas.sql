-- Las ordenes que ya estaban cobradas antes de que la orden guardara su
-- propia marca. Sin esto no se podrian volver a entregar.
UPDATE ordenes_trabajo SET pagada = TRUE WHERE estado = 'ENTREGADO';
