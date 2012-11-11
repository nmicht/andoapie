<?php
$completeurl = "kml/ruta.kml";
$xml = simplexml_load_file($completeurl);

$name = $xml->Document->name->asXML();

$placemarks = $xml->Document->Placemark;

for ($i = 0; $i < sizeof($placemarks); $i++){
    $coordinate = $placemarks[$i]->LineString->coordinates;
	$ruta[] = processXML($coordinate->asXML(),"LineString");
}

$placemarks = $xml->Document->Folder->Placemark;

for ($i = 0; $i < sizeof($placemarks); $i++){
    $coordinate = $placemarks[$i]->Point->coordinates;
	$terminal[] = processXML($coordinate->asXML(),"Point");
}

//echo $ruta[0],$ruta[1],$terminal[0],$terminal[1];
saveRoute($nombre,$ruta[0],$ruta[1],$terminal[0],$terminal[1]);

/**
  * @param string $cad with all the coordinates for a linestring or a point
  * @return string of a JSON
  * This function convert the string in arrays and then generate the JSON format
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

/**
  * This function save on a database the info of the route
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

	//Ejecutar el query
	$conexion -> query($query);

}


?>
