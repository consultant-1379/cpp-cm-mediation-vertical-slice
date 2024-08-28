#! /bin/bash
#
# ebialan
# 
# 
source docker-env-functions.sh

export_emf() {
    local DPS_INTEGRATION_EGT_OUTPUT_FOLDER="/opt/ericsson/ERICdpsupgrade/egt/output/"   
    cd $DPS_INTEGRATION_EGT_OUTPUT_FOLDER 
    local EAR_NAME=$(ls -m1 dps-jpa-ear*.ear)
    mkdir -p tmp && cd tmp && jar xf ../$EAR_NAME && cd lib
    local JAR_NAME=$(ls -m1 dps-entities-*.jar)
    mkdir -p tmp && cd tmp && jar xf ../$JAR_NAME && cd META-INF
    awk '{if ($1 ~ /<properties>/) print $0, "\n\t\t\t<property name=\"jboss.entity.manager.factory.jndi.name\" value=\"java:jboss/versant-emf\" />"; else print $0}' persistence.xml > persistence_new.xml && mv persistence_new.xml persistence.xml
    cd ../.. && jar -uf $JAR_NAME -C tmp META-INF/persistence.xml
    cd ../.. && jar -uf $EAR_NAME -C tmp lib/$JAR_NAME
    rm -rf tmp
    ls -la $DPS_INTEGRATION_EGT_OUTPUT_FOLDER
}


install_rpms_from_nexus
install_rpms_from_iso
cleanup_deployment
copy_jboss_config
wait_dps_integration
export_emf

