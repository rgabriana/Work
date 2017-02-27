Author :  Sachin Khadilkar

Procedure to create Enlighted Server 14.04 ISO 
===============================================

1. First download Ubuntu 14.04 AMD-64 or Ubuntu 14.04 AMD-32 Server version ISO from a Ubuntu mirror closest to you.

2. Install the Ubuntu Server 14.04 on a PC or VM.

On the PC or VM Running the Ubuntu Server :
3. Now extract the contents of the iso into a folder called "newiso" in your home folder : 
   To do this you will have to mount the ISO and then copy the contents from the mounted folder into the "newiso" folder,
   alternatively you can use the 7z utility to directly extract the contents of the iso into the "newiso" folder.
   MAKE SURE YOU COPY THE .disk FOLDER FROM THE VANILLA UBUNTU ISO OTHERWISE IT SIMPLY REFUSES TO INSTALL.

4. Copy the preseed and scripts folders adjacent to this README.txt file into you home folder. In case you are creating a 32-bit version
   There is a folder called 32-bit adjacent to this README.txt file which contains the preseed and scripts folders.

5. Remove all files from /var/cache/apt/archives folder.

6. Now install all the packages mentioned in the preseed/ubuntu-myownoption.seed file at line 228 : d-i pkgsel/include like openssh-server, openssl etc.,
   this would download the debian's and all dependant debian's in the /var/cache/apt/archives folder.
   
7. Create a folder called "extras" under newiso/pool folder. Copy the *.deb files from /var/cache/apt/archives folder into newiso/pool/extras folder.

8. Replace the preseed folder in newiso with the preseed folder adjacent to this README.txt file.

10. Create a folder called extras under newiso/dists/trusty. Then create folders "binary-amd64" and "binary-i386" under newiso/dists/trusty/extras folder.

11. To create Packages and Packages.gz under the newiso/dists/trusty/extras/binary-amd64 folder run the following command :
    cd ~/newIso
	sudo apt-ftparchive packages ./pool/extras/ > ~/Packages
	cd ..
	sudo gzip -c Packages | tee Packages.gz > /dev/null


	sudo cp Packages dists/stable/extras/binary-amd64/
	sudo cp Packages.gz dists/stable/extras/binary-amd64/
	sudo cp Packages dists/stable/extras/binary-i386/
	sudo cp Packages.gz dists/stable/extras/binary-i386/


12. Now to create the md5 checksum file run the md5sum.sh script in the scripts folder adjacent to this README.txt file.

13. Last step is to run the mkisofs.sh script in the scripts folder adjacent to this README.txt file. 
    This will create the "EnlightedServer1404_Base.iso" file in the current folder.
   


