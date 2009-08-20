# See: http://java.sun.com/javase/6/docs/technotes/tools/windows/keytool.html#genkeypairCmd
jks=wol.jks
pass=test1234
certName=wol
dn="cn=EnerNOC R&D, ou=Engineering, o=EnerNOC Inc., l=Boston, ST=Massachusetts, c=US"

# This will produce a self-signed certificate usable for signing applets.
keytool -genkeypair -keysize 1024 -keyalg RSA -alias $certName -keystore $jks \
  -storepass $pass -validity 1095 \
-dname "$dn"
#keytool -list -v -keystore $jks
#keytool -certreq -alias $certName -keypass $pass -keystore $jks -file wol.csr 