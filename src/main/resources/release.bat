@rem launch4j is there : http://sourceforge.net/project/showfiles.php?group_id=95944&package_id=152236
@rem izpack is there : http://prdownload.berlios.de/izpack/IzPack-install-3.10.2.jar
@gpg is there : http://ftp.gnupg.org/GnuPG/binary/

mvn clean firemox:maven-docgen-plugin:1.0-SNAPSHOT:docgen
gpg --gen-key 
@rem install with launch4j enabled and izpack disabled
mvn install -Dgpg.passphrase=thephrase
mvn assembly:assembly

@rem install with izpack enabled and launch4j disabled
mvn install site -Dgpg.passphrase=thephrase