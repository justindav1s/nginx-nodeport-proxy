#!/usr/bin/env groovy

@Library( [ 'jenkins-path-to-prod-library@1.0.10' ] ) _

import uk.co.mo.sepg.p2p.SyncPipelineWrapper
import uk.co.mo.sepg.p2p.helper.BaseHelper

SyncPipelineWrapper wrapper = new SyncPipelineWrapper()

wrapper.runPipeline( [
        applicationName: 'cpu-tcp-proxy',
        namespace      : "cpu-tcp-${ env.ENVIRONMENT }",
        deployFunction : { Map arguments -> runCustomDeployStages( arguments ) },

        fileExtensionsForPropertyValueFiles: [ 'conf' ],
] )

def runCustomDeployStages( Map arguments = [ : ] as Map ) {
    info( 'runCustomDeployStages', 'starting' )

    BaseHelper          baseHelper          = new BaseHelper()
    SyncPipelineWrapper syncPipelineWrapper = new SyncPipelineWrapper()

    baseHelper.stageWrapper( 'Apply Secrets', {
        applyConfigurationFrom1Password( arguments )
    } )

    syncPipelineWrapper.runDefaultDeploymentStages( arguments )

    info( 'runCustomDeployStages', 'finishing' )
}

private def info( String methodName, String log ) {
    println "CustomSyncPipeline.${ methodName }: ${ log }"
}

def applyConfigurationFrom1Password( Map arguments = [ : ] as Map ) {
    String TEMPLATES_FOLDER = 'configuration/templates'

    String applicationName = getRequiredParameter( arguments, 'applicationName' )
    String namespace       = getRequiredParameter( arguments, 'namespace'       )

    String session = signInTo1password( onePasswordCredentialsNamespace: env.JENKINS_NAMESPACE )

    String source = "${ TEMPLATES_FOLDER }/environment-specific-config/cpu-tcp-credentials.conf"                      as String
    String target = "${ TEMPLATES_FOLDER }/environment-specific-config/${ env.ENVIRONMENT }/cpu-tcp-credentials.conf" as String

    Map itemsPatterns = [
            'cpu-tcp-cloud-webservices': 'ALFA_WEBSERVICES_CREDENTIALS',
            'cpu-tcp-cloud-scheduler'  : 'ALFA_SCHEDULER_CREDENTIALS',
    ] as Map

    itemsPatterns.each { String item, String pattern ->
        replacePatternWithEncodedValueInBase64( [
                source : source,
                target : target,
                item   : item,
                vault  : namespace,
                session: session,
                pattern: pattern,
        ] as Map )
    }

    Map< String, Object > secretParameters = [
            RESOURCE_NAME              : applicationName,
            APP_LABEL                  : env.APPLICATION_LABEL,
            PROXY_ALFA_CREDENTIALS_CONF: getOutputFromShellCommand( "cat ${ target }" ),
    ] as Map< String, Object >

    applyResourceFromTemplate(
            namespace           : namespace,
            resourceTemplateFile: "${ TEMPLATES_FOLDER }/secret.yaml",
            parameters          : secretParameters,
            hideOutput          : true,
    )
}

def replacePatternWithEncodedValueInBase64( Map arguments = [ : ] as Map ) {
    String source  = arguments.get( 'source'  )
    String target  = arguments.get( 'target'  )
    String item    = arguments.get( 'item'    )
    String vault   = arguments.get( 'vault'   )
    String session = arguments.get( 'session' )
    String pattern = arguments.get( 'pattern' )

    String command = """\
        op get item ${ item } --vault=${ vault } --session=${ session } > ${ item }.json

        username=\$( cat ${ item }.json | jq -r '.details | select( .fields != null ) | .fields[] | select( .name == "username" ).value' )
        password=\$( cat ${ item }.json | jq -r '.details | select( .fields != null ) | .fields[] | select( .name == "password" ).value' )
        encoded=\$( printf \${username}:\${password} | base64 -w 0 )

        sed "s:<${ pattern }>:\${encoded}:g" ${ source } > ${ target }
        cp -fpv ${ target } ${ source }
    """ as String

    getOutputFromShellCommand( command )
}
