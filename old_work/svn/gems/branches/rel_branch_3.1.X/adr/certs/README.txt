Following are the two sample certs downloaded from the "OpenADR 2 0a Test Suite Code Drop v1_0_4"

#Trust and client key store
DUT_Keystore.jks   
DUT_Truststore.jks 

Pre-requirement: Based on using certs from TH_Signed_Certs
--------------------------------------------------------------------------------
Alias name: signed_ecc
#1: ObjectId: 2.5.29.17 Criticality=false
SubjectAlternativeName [
  IPAddress: 192.168.5.100
]

Alias name: signed_rsa
#1: ObjectId: 2.5.29.17 Criticality=false
SubjectAlternativeName [
  IPAddress: 192.168.5.100
]

Options:
1) Either make VTN run on 192.168.5.100 
2) Or make entry for 127.0.0.1 as signed_ecc in the /etc/hosts file (either one is checked for verification at the handshake time)
