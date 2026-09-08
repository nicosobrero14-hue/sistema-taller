package com.tallerapp.taller_service.service;

import java.math.BigDecimal;
import java.util.List;

import com.tallerapp.taller_service.entity.ItemOrdenTrabajo;
import com.tallerapp.taller_service.entity.TipoItem;

public interface IItemOrdenTrabajoService {
	
	public String saveItem(Long id_orden, TipoItem tipo, Long id_repuesto, String descripcion, Integer cantidad, BigDecimal precioUnitario);
	public List<ItemOrdenTrabajo> getItems();
	public String deleteItem(Long id);
	public ItemOrdenTrabajo findItem(Long id);
	public String editItem(Long id, ItemOrdenTrabajo item);

}
