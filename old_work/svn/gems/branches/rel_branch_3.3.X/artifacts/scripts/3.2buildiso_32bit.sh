#!/bin/bash
set +e


ROOTDIR=`pwd`
VERSION="$1"
BUILDDATE=`date '+%Y%m%d-%H%M%S'`
LIVEDIR=`echo release $VERSION $BUILDDATE|tr -s ' ' '_'`

if [ ! -d $ROOTDIR/images ]; then
	mkdir -pv $ROOTDIR/images
fi
mkdir $ROOTDIR/$LIVEDIR
#cp $ROOTDIR/EnlightedServer1404_32bit__Base.iso $ROOTDIR/$LIVEDIR
cp $ROOTDIR/em_all.deb $ROOTDIR/$LIVEDIR
cp $ROOTDIR/*.sh $ROOTDIR/$LIVEDIR
sudo cp $ROOTDIR/sudoers $ROOTDIR/$LIVEDIR

cd $ROOTDIR/$LIVEDIR
 
mkdir mnt
sudo mount -o loop /home/bldadm/workspace/downloads/EnlightedServer1404_32bit__Base.iso mnt
mkdir extract-cd
echo $VERSION GEMS > extract-cd/imageversion
rsync --exclude=/install/filesystem.squashfs -a mnt/ extract-cd

sudo unsquashfs mnt/install/filesystem.squashfs
sudo mv squashfs-root edit
sudo cp $ROOTDIR/$LIVEDIR/*.deb $ROOTDIR/$LIVEDIR/edit/tmp
sudo cp $ROOTDIR/$LIVEDIR/*.sh $ROOTDIR/$LIVEDIR/edit/tmp
sudo chmod +x $ROOTDIR/$LIVEDIR/edit/tmp/*.sh
sudo cp /etc/resolv.conf edit/etc
sudo cp /etc/hosts edit/etc
sudo cp $ROOTDIR/$LIVEDIR/sudoers edit/etc
sudo chown root:root edit/etc/sudoers

sudo mount --bind /dev edit/dev
sudo umount mnt

sudo chroot edit /tmp/3.2buildiso2_32bit.sh

sudo umount $ROOTDIR/$LIVEDIR/edit/dev
chmod +w extract-cd/install/filesystem.manifest
sudo chroot edit dpkg-query -W --showformat='${Package} ${Version}\n' > extract-cd/install/filesystem.manifest
sudo cp extract-cd/install/filesystem.manifest extract-cd/install/filesystem.manifest-desktop
sudo sed -i '/ubiquity/d' extract-cd/install/filesystem.manifest-desktop
sudo sed -i '/install/d' extract-cd/install/filesystem.manifest-desktop
if [ -f extract-cd/install/filesystem.squashfs ]; then
	sudo rm extract-cd/install/filesystem.squashfs
fi
sudo mksquashfs edit extract-cd/install/filesystem.squashfs
cd extract-cd
sudo rm md5sum.txt
find -type f -print0 | sudo xargs -0 md5sum | grep -v isolinux/boot.cat | sudo tee md5sum.txt
sudo mkisofs -D -r -V "GEMS" -cache-inodes -J -l -b isolinux/isolinux.bin -c isolinux/boot.cat -no-emul-boot -boot-load-size 4 -boot-info-table -o $ROOTDIR/images/$LIVEDIR.iso .
cd $ROOTDIR/$LIVEDIR
sudo rm -rf extract-cd
sudo rm -rf edit
cd $ROOTDIR
