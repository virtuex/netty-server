#!/bin/bash
function gencert() {
	if [[ ! -d /etc/cas ]] ; then
		copy
	fi
	which keytool
	if [[ $? -ne 0 ]] ; then
		echo Error: Java JDK \'keytool\' is not installed or is not in the path
		exit 1
	fi
	# override DNAME and CERT_SUBJ_ALT_NAMES before calling or use dummy values
	DNAME="${DNAME:-CN=cas.example.org,OU=Example,OU=Org,C=US}"
	CERT_SUBJ_ALT_NAMES="${CERT_SUBJ_ALT_NAMES:-dns:example.org,dns:localhost,ip:127.0.0.1}"
	echo "Generating keystore for CAS with DN ${DNAME}"
	keytool -genkeypair -alias cas -keyalg RSA -keypass changeit -storepass changeit -keystore /etc/cas/thekeystore -dname ${DNAME} -ext SAN=${CERT_SUBJ_ALT_NAMES}
	keytool -exportcert -alias cas -storepass changeit -keystore /etc/cas/thekeystore -file /etc/cas/cas.cer
}

if [ $# -eq 0 ]; then
    echo -e "No commands provided. Defaulting to [run]\n"
    run
    exit 0
fi

case "$1" in
"gencert")
    gencert "$@"
    ;;
*)
    help
    ;;
esac