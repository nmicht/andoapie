<?php
$completeurl = "kml/ruta.kml";
$xml = simplexml_load_file($completeurl);

$placemarks = $xml->Document->Placemark;
 
$coordinate = $placemarks[0]->LineString->coordinates;

$pares = processXML($coordinate->asXML());

function processXML($cad){
	$datos["type"] = "LineString"; 
	$a = split(' ',$cad);
	foreach($a as $coord)
		$datos["coordinates"][] = array_map('floatval', explode(',',$coord));

	echo json_encode($datos);
}
?>
