<?php
$completeurl = "kml/ruta.kml";
$xml = simplexml_load_file($completeurl);

$placemarks = $xml->Document->Placemark;
 
for ($i = 0; $i < sizeof($placemarks); $i++) {
    $coordinates = $placemarks[$i]->LineString->coordinates;
 
    echo $coordinates,'<br />';
}
?>
