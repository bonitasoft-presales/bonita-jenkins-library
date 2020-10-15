#!groovy


/**
 * Ensure a string variable is defined and not empty.
 * Fail the build with an error message otherwise.
 */
def ensure_stringvar(var, error_message) {
    if (var == null || var.trim().isEmpty()) {
        error "[bcd-lib] ${error_message}"
    }
}

/**
 * Invoke the `bcd` command using a proper shell script.
 *
 * @param args String arguments to the `bcd` command
 * @param scenario Path to scenario file
 */
def invoke_bcd(args,scenario) {

    // ensure args
    ensure_stringvar(args, "Arguments are empty! Did you forget to provide arguments to the 'bcd' step?")

    // ensure scenario
    ensure_stringvar(scenario, "Mandatory argument 'scenario' is not set!")
    def bcd_cmd = "bcd -s ${scenario} -y ${args}"
    echo "[bcd-lib] ${bcd_cmd}"

    // execute bcd with bash
    sh """#!/bin/bash -l
set -euo pipefail

medium_echo() {
    echo "___________________________________________________________________________________________________________"
    echo "\$1"
}

medium_echo "Bonita Continuous Delivery for Jenkins!"
bcd version

echo "env variables:"
echo \${PATH}
echo \${JAVA_HOME}


java -version


cd \${BCD_HOME}
${bcd_cmd}
"""
}

/**
 * Make 'bcd' a Jenkins Pipeline step.
  *
 * Configuration: 
 *   args : String arguments to the `bcd` command
 *   ignore_errors : Boolean flag to not fail the `bcd` command call uppon errors (default to false)
 *   scenario : Path to scenario file
 *
 *
 *
 * Example usage with a scripted pipeline:
 *
 *   node('bcd') {
 *     stage('Deploy Bonita Server') {
 *       bcd scenario: myScenario.yml args: 'stack create', ignore_errors: true
 *       bcd scenario: myScenario.yml args: 'stack undeploy', ignore_errors: true
 *       bcd scenario: myScenario.yml args: 'stack deploy'
 *     }
 *
 *     stage('Build Bonita LivingApp') {
 *       bcd scenario: myScenario.yml args: "livingapp build -p ${WORKSPACE} -e Qualification"
 *     }
 *   }
 *
 */
def call(Map config) {
    try {
        invoke_bcd(config.args,config.scenario)
    }
    catch (err) {
        if (config.ignore_errors) {
            echo """[bcd-lib] bcd call failed: ${err}
...ignore failure and continue..."""
        } else {
            throw err
        }
    }
}
