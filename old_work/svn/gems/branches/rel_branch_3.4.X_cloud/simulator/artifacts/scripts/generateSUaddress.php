<?php
        for ($i = 0; $i < 1; $i++) {
                for ($j = 1; $j < 2; $j++) {
                        for ($k = 0; $k < 250; $k++) {
				if($k > 0) {
					echo ",";
				}
                                echo dechex($i) . ":" . dechex($j) . ":" . dechex($k);
                        }
                }
        }
?>
