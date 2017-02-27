cd newiso/
sudo apt-ftparchive packages ./pool/extras/ > ../Packages
cd .. 
sudo gzip -c Packages | tee Packages.gz > /dev/null
sudo mv Packages* newiso/dists/trusty/extras/binary-i386/
cd newiso
sudo md5sum `find ! -name .md5sum.txt. ! -path ../isolinux/*. -follow -type f` > ~/md5sum.txt
sudo mv ~/md5sum.txt .
cd ..
sudo mkisofs -J -l -b isolinux/isolinux.bin -no-emul-boot -boot-load-size 4 -boot-info-table -z -iso-level 4 -c isolinux/isolinux.cat -o ./EnlightedServer1404_32bit__Base.iso -joliet-long newiso/
