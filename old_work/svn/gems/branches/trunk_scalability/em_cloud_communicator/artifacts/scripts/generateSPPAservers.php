<?php
	echo "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" . "\n";
	echo "<sppa>" . "\n";
	echo "<server>sppa.enlightedinc.com</server>" . "\n";
	echo "<emlist>" . "\n\t";

        for ($i = 1; $i < 2; $i++) {
                for ($j = 1; $j < 5; $j++) {
                        for ($k = 0; $k < 255; $k++) {
				echo "\t<em>" . "\n\t";
				echo "\t\t<mac>";
                                echo "68:54:f1" . ":" . dechex($i) . ":" . dechex($j) . ":" . dechex($k);
				echo "</mac>" . "\n\t";
				echo "\t\t<version>2.2.0.201</version>" . "\n\t";
				echo "\t</em>" . "\n\t";
                        }
                }
        }
	echo "</emlist>" . "\n";
	echo "</sppa>";
?>
