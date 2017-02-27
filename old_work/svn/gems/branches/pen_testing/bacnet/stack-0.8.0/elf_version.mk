#Make 3 for release
ifndef MAJOR_VERSION
   MAJOR_VERSION = 0
endif
ifndef MINOR_VERSION
   MINOR_VERSION = 1
endif
ifndef RELEASE_VERSION
   RELEASE_VERSION = 2
endif
# BUILD_VERSION = $(shell svn info | grep 'Last Changed Rev:' | cut -d ' ' -f4)
# todo
ifndef BUILD_VERSION
   BUILD_VERSION = 99
endif

ELF_VERSION = ${MAJOR_VERSION}.${MINOR_VERSION}.${RELEASE_VERSION}.${BUILD_VERSION}
