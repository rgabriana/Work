include stack-0.8.0/elf_version.mk

installHVACpkg: HVAC
	cp stack-0.8.0/demo/gateway/bacnetHVAC install/bacnetd
	cp enlighted/config/Enlighted_EM/bacnet_objects.cfg install
	cp enlighted/config/Enlighted_EM/bacnet.conf install
	chmod 777 install/bacnet
	chmod 777 install/install.sh
	tar -zcvf install/bacnet_${ELF_VERSION}.tgz install/bacnetd install/bacnet_objects.cfg install/bacnet.conf install/bacnet  install/curl_7.19.7-1ubuntu1.1_i386.deb install/install.sh install/uninstall.sh install/libroxml-2.2.2.tar.gz install/procmon.cron install/procmon.sh

installpkg: bacnetLighting
	cp stack-0.8.0/demo/gateway/bacnetLighting install/bacnetd
	cp enlighted/config/Enl_EM_Expanded/bacnet_objects.cfg install
	cp enlighted/config/Enl_EM_Expanded_install/bacnet.conf install
	chmod 777 install/bacnet
	chmod 777 install/install.sh
	chmod 777 install/setHBtrigger.sh
	tar -zcvf install/bacnet_${ELF_VERSION}.tgz install/bacnetd install/bacnet_objects.cfg install/bacnet.conf install/bacnet  install/curl_7.19.7-1ubuntu1.1_i386.deb install/install.sh install/uninstall.sh install/libroxml-2.2.2.tar.gz install/procmon.cron install/procmon.sh install/toxml.sed install/setHBtrigger.sh

bacnetLighting:
	make -C stack-0.8.0 ENL_TARGET=Lighting

all:	
	# do nothing for all, it does not make sense in our multiple-project build 

HVAC: 
	make -C stack-0.8.0 ENL_TARGET=HVAC 
	
Lighting: 
	make -C stack-0.8.0 ENL_TARGET=Lighting 
	
clean:
	rm -f install/bacnet.tar
	make -C stack-0.8.0 clean
	
