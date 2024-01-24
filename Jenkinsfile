pipeline {
    agent any

    environment {
        VAULT_ADDR = 'http://localhost:8201'
        
        VAULT_CREDENTIALS = 'my-vault-token'
    }

    stages {
        stage('Preparation') { // Запустить перед вытаскиванием секрета
            steps {
                echo 'Preparing the environment...'
            }
        }
        
        stage('Get Secret') {
            steps {
                withCredentials([string(credentialsId: "${env.VAULT_CREDENTIALS}", variable: 'VAULT_TOKEN')]) {
                    script {
                        // Use Vault CLI Пример использования CLI, можно использовать также API
                        def secrets = sh(script: "vault read -field=data -format=json secret/myapp/config", returnStdout: true).trim()
                        echo "Secrets: ${secrets}"
                        
                        // Парсинг JSON ответа для извлечения конкретных секретов
                        def parsedSecrets = readJSON text: secrets
                        env.MYAPP_USERNAME = parsedSecrets.username
                        env.MYAPP_PASSWORD = parsedSecrets.password
                    }
                }
            }
        }

        stage('Use Secret') { // Использовать секрет
            steps {
                echo "Username is: ${env.MYAPP_USERNAME}"
                echo "Password is: ${env.MYAPP_PASSWORD}"
            }
        }

        stage('Post-processing') { // Запустить после использования секрета
            steps {
                echo 'Cleaning up...'
            }
        }
    }
}
