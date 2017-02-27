<?php

function isValidInterface($input){
        $patternList=array("/^eth/","/^p[0-9]/");
        foreach($patternList as $pattern){
                 if(  (preg_match($pattern, $input)) ){
                        return 1;
                }
        }
        return 0;
}

$fp = popen (" ls /sys/class/net | grep -v \"lo\" ", "r");
 $all_interfaces = array();
 while ($rec = fgets($fp)){
     $iface = trim($rec);
     if( isValidInterface($iface) ){
        $all_interfaces[] = $iface;
     }
 }

class InterfaceDetail{
        public $name = "";
        public $macaddress = "";
        public $ipaddress = "";
        public $subnet_mask = "";
        public $default_gateway = "";
        public $dns = "";
        public $search_domain_fields = "";
        public $connected_status = "";
        public $is_dhcp_server = "";
}
$jsonArray = array();

function getShelloutput($cmd){
	$out = shell_exec($cmd);
	if ( is_null($out) ){
		return null;
	}else{
		return trim($out);
	}
}
foreach($all_interfaces as $val) {
        $obj = new InterfaceDetail();
        $obj->name = $val;
        
		$obj->macaddress = getShelloutput('cat /sys/class/net/'.$val.'/address');
        $obj->ipaddress = getShelloutput('sudo ifconfig '.$val.' | grep inet | sed -n \'s/^\s*inet addr://p\' | awk \'{ print $1 }\'');
        $obj->subnet_mask = getShelloutput('sudo ifconfig '.$val.' | grep Mask | sed -e \'s/.*Mask://\'');
        $obj->default_gateway = getShelloutput('netstat -rn  | grep '.$val.' | grep UG | awk \'{print $2}\'');
		
		// dns related parameters are by default made empty string as since as of now we dont know proper command how to retrieve its values        
        $obj->dns = "";
        $obj->search_domain_fields = "";
        
        $obj->connected_status = getShelloutput('cat /sys/class/net/'.$val.'/operstate');
        $output = getShelloutput('cat /etc/network/interfaces | grep '.$val.' | grep dhcp | wc -l');
        $is_dhcp = false;
        if ( $output > 0 ){
                 $is_dhcp = true;
        }
        $obj->is_dhcp_server = $is_dhcp;
        $jsonArray[] = ($obj);
}

echo json_encode(array('interfaces' => array_values($jsonArray)));
        
