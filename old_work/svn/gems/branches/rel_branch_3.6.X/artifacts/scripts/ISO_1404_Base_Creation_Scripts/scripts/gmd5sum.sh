cd googleiso
sudo md5sum `find ! -name .md5sum.txt. ! -path ../isolinux/*. -follow -type f` > ~/md5sum.txt
sudo mv ~/md5sum.txt .
cd ..
