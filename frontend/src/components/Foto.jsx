import { useEffect, useState } from "react";
import { URL_API, traerSesion } from "../api";

/**
 * Una etiqueta <img> no puede mandar la cabecera con el token, asi que
 * el gateway le contestaria 401 y la foto no se veria. Por eso la imagen
 * se baja con fetch (que si lleva el token) y se muestra desde la memoria
 * del navegador.
 */
function Foto({ archivo, mini, alt }) {
  const [url, setUrl] = useState(null);

  useEffect(() => {
    const sesion = traerSesion();
    let creada = null;

    const ruta = mini ? "/fotos/mini/" + mini : "/fotos/ver/" + archivo;

    fetch(URL_API + ruta, {
      headers: sesion ? { Authorization: "Bearer " + sesion.token } : {},
    })
      .then((res) => (res.ok ? res.blob() : null))
      .then((blob) => {
        if (blob) {
          creada = URL.createObjectURL(blob);
          setUrl(creada);
        }
      })
      .catch(() => setUrl(null));

    // al desmontar se libera, si no el navegador se va llenando de imagenes
    return () => {
      if (creada) {
        URL.revokeObjectURL(creada);
      }
    };
  }, [archivo, mini]);

  if (!url) {
    return <div className="foto-cargando" />;
  }

  return <img src={url} alt={alt || ""} />;
}

export default Foto;
