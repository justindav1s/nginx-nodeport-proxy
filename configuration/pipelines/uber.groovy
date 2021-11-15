#!/usr/bin/env groovy

@Library( [ 'jenkins-path-to-prod-library@1.0.10' ] ) _

import uk.co.mo.sepg.p2p.BaseUberPipelineWrapper
import uk.co.mo.sepg.p2p.helper.BaseHelper

new BaseUberPipelineWrapper().runPipeline( [
        applicationName : 'cpu-tcp-proxy',
        applicationLabel: 'cpu-tcp-proxy',
        buildNodeLabel  : 'mo-aio-small-8.12',
        buildFunction   : { Map arguments -> runCustomBuildStages( arguments ) },

        nonProdEnvironments : [ 'dev', 'e2e', 'uat', 'bau' ],
        nonProdTargetCluster: 'OCP4', // ToDo: Could it be BOTH?

        includeScanForVulnerabilitiesStage: 'false',
] )

def runCustomBuildStages( Map arguments = [ : ] as Map ) {
    info( 'runCustomBuildStages', 'starting' )

    BaseHelper              baseHelper          = new BaseHelper()
    BaseUberPipelineWrapper uberPipelineWrapper = new BaseUberPipelineWrapper()

    uberPipelineWrapper.initializeGitAndStashes( arguments )

    baseHelper.stageWrapper( 'Build Artifact', {
        println "This is S2I build, there's no artifact to build..."
    } )

    uberPipelineWrapper.runScanForVulnerabilitiesStageIfEnabled( arguments )

    baseHelper.stageWrapper( 'Build Image', {
        try {
            milestone(10)

            buildImage( arguments )
        } finally {
            sendBuildInfoToJira()
        }
    } )

    info( 'runCustomBuildStages', 'finishing' )
}

private def info( String methodName, String log ) {
    println "CustomUberPipeline.${ methodName }: ${ log }"
}
