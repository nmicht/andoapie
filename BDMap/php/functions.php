<?php

/**
 * @param string $nombre
 * @param string $ida the coordinates for the linestring
 * @param string $vuelta the coordinates for the linestring
 * @param string $terminal the coordinates for the point
 * @param string $retorno the coordinates for the point
 * @return boolean, true in case of succesful
 */
function saveRoute($nombre, $ida, $vuelta, $terminal, $retorno){
	//Connecting to the database	
	require_once("php/bd.inc");
	$conexion = new mysqli($dbhost, $dbuser, $dbpass, $db);

	//Finish the execution if the connection fail
	if ($conexion->connect_error) {
		die('Error de Conexión (' . $conexion->connect_errno . ') '
		        . $conexion->connect_error);
	}

	//Creating the query
	$query = "INSERT INTO ruta
				(nombre,
				derroteroVuelta,
				derroteroIda,
				terminal,
				terminalRetorno)
			VALUES
				(
				'$nombre',
				'$ida',
				'$vuelta',
				'$terminal',
				'$retorno'
				)
			";

	//Execute the query
	$conexion -> query($query);

	//close conecction
	$conexion -> close();
}

/**
 * @param int $id of the route
 * @return array $datos with all the info for the route
 */
function obtainRoute($id){
	//Connecting to the database	
	require_once("bd.inc");
	$conexion = new mysqli($dbhost, $dbuser, $dbpass, $db);

	//Finish the execution if the connection fail
	if ($conexion->connect_error) {
		die('Error de Conexión (' . $conexion->connect_errno . ') '
		        . $conexion->connect_error);
	}

	//Creating the query
	$query = "SELECT *
			  FROM ruta
			  WHERE idruta=$id";

	//Execute the query
	$result = $conexion -> query($query);

	//close conection
	$conexion -> close();

	//Convert the result in an array
	if($result -> num_rows == 1)
		$datos = $result -> fetch_array(MYSQLI_ASSOC);

	return $datos;
}

/**
 * Read the kml files and save it in the database as text
 */
function processKMLFiles(){
	//Ciclo para todos los archivos
	$path="../kml/";
	$directorio=dir($path);
	while ($archivo = $directorio->read()){
		$completeurl = $archivo;
		$xml = simplexml_load_file($completeurl);

		$name = $xml->Document->name->asXML();

		$placemarks = $xml->Document->Placemark;

		for ($i = 0; $i < sizeof($placemarks); $i++){
			$coordinate = $placemarks[$i]->LineString->coordinates;
			$ruta[] = $coordinate->asXML();
			//$ruta[] = processXML($coordinate->asXML(),"LineString");
		}

		$placemarks = $xml->Document->Folder->Placemark;

		for ($i = 0; $i < sizeof($placemarks); $i++){
			$coordinate = $placemarks[$i]->Point->coordinates;
			$terminal[] = $coordinate->asXML();
			//$terminal[] = processXML($coordinate->asXML(),"Point");
		}

		//echo $ruta[0],$ruta[1],$terminal[0],$terminal[1];
		saveRoute($nombre,$ruta[0],$ruta[1],$terminal[0],$terminal[1]);
	}
	$directorio->close();
}


/**
 * @param string $cad with all the coordinates for a linestring or a point
 * @param string $object indicate the type of coordinates, linestring or point
 * @return string with the GEOJSON
 * This function proccess an array and convert the strings to generate GEOJSON format
 */ 
function processXML($cad,$object){

	$datos["type"] = $object; 
	$a = split(' ',$cad);

	//to remove the leading whitespace
	if($object == "LineString")
		array_pop($a);

	foreach($a as $coord)
		$datos["coordinates"][] = array_map('floatval', explode(',',$coord));

	return json_encode($datos);
}



?>

