pipeline {
    agent any
    
    options {
        quietPeriod(120)
        // Log-rotator instellingen overgenomen uit de oude XML (40 dagen bewaren, max 69 builds)
        buildDiscarder(logRotator(daysToKeepStr: '40', numToKeepStr: '69'))
    }
    
   triggers {
        GenericTrigger(
            genericVariables: [
                [key: 'ref', value: '$.ref']
            ],
            token: 'servoy-mobile',
            regexpFilterText: '$ref',
            regexpFilterExpression: "^refs/heads/${env.BRANCH}\$"
        )
    }
    
    parameters {
        string(name: 'goals', defaultValue: 'install', trim: false)
    }
    
    environment {
        TEAMS_WEBHOOK = credentials('servoy-teams-webhook')
    }
    
    tools {
        jdk 'Java 21' // Uniform meegetrokken naar Java 21
        maven 'Maven 3.9.16'
    }
    
    stages {
        stage('Build Mobile') {
            steps {
                configFileProvider([
                    configFile(fileId: 'master_mvn_repo', variable: 'MAVEN_SETTINGS'),
                    configFile(fileId: '254658cc-4d79-45bf-ace9-28bd69fd403d', variable: 'TOOLCHAIN')
                ]) {
                    sh 'mvn -B -s "$MAVEN_SETTINGS" -t "$TOOLCHAIN" $goals'
                }
            }
        }
    }
    
    post {
        always {
            // Testresultaten verzamelen (AggregatedTestResultPublisher)
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
        
        failure {
            // Veilige Teams notificatie zonder quotes
            office365ConnectorSend webhookUrl: TEAMS_WEBHOOK, status: 'Failed', adaptiveCards: true
            
            // Mail naar developers bij falen
            emailext body: '$PROJECT_DEFAULT_CONTENT', 
                     subject: '$PROJECT_DEFAULT_SUBJECT', 
                     replyTo: '$PROJECT_DEFAULT_REPLYTO', 
                     recipientProviders: [developers()]
        }
        
        unstable {
            office365ConnectorSend webhookUrl: TEAMS_WEBHOOK, status: 'Unstable', adaptiveCards: true
        }
        
        fixed {
            office365ConnectorSend webhookUrl: TEAMS_WEBHOOK, status: 'Back to Normal', adaptiveCards: true
        }
    }
}