#!/usr/bin/env groovy
@Grab('org.jfrog.automation.xray:xray-groovy-client:0.1.12-UNIFIED')

import org.jfrog.automation.xray.XrayAPI
import org.jfrog.automation.xray.consts.PolicyType
import org.jfrog.automation.xray.consts.Severity
import org.jfrog.automation.xray.consts.TargetType
import org.jfrog.automation.xray.exception.XrayRestClientException
import org.jfrog.automation.xray.model.v2.policy.*
import org.jfrog.automation.xray.model.v2.watch.XrayWatchModel
import org.jfrog.automation.xray.model.v2.watch.XrayWatchResourceModel

class Main {
    static void main(String... args) {

        XrayAPI client = new XrayAPI("http://10.70.30.83:8082/xray", "admin", "Dn2120091")

        def pingStatus = client.v1().system().ping().status
        println("Xray Server status response: ${pingStatus}")

        if(pingStatus == 'pong') {

            BlockDownloadModel bDownload = new BlockDownloadModel(true, true)

            XrayPolicyRulesCriteriaModel criteriaModel = new XrayPolicyRuleSeverityCriteriaModel(Severity.HIGH)

            XrayPolicyActionsModel action = new XrayPolicyActionsModel(
                    bDownload,
                    true,
                    true,
                    true,
                    true,
                    [] as ArrayList<String>,
                    Severity.HIGH
            )

            XrayPolicyRuleLicenseCriteriaModel criteriaModel2 = new XrayPolicyRuleLicenseCriteriaModel(
                    [] as ArrayList<String>,
                    ["ADSL"] as ArrayList<String>,
                    false
            )

            XrayPolicyRulesModel rule1 = new XrayPolicyRulesModel(
                    "r1",
                    1,
                    criteriaModel2,
                    action
            )

            XrayPolicyRulesModel rule2 = new XrayPolicyRulesModel(
                    "r1",
                    1,
                    criteriaModel,
                    action
            )

            XrayPolicyModel policy1 = new XrayPolicyModel(
                    "lic",
                    "Description",
                    PolicyType.LICENSE,
                    [rule1] as ArrayList<XrayPolicyRulesModel>
            )

            XrayPolicyModel policy2 = new XrayPolicyModel(
                    "sec",
                    "Description",
                    PolicyType.SECURITY,
                    [rule2] as ArrayList<XrayPolicyRulesModel>
            )

            XrayWatchResourceModel resource = new XrayWatchResourceModel(
                    targetType: TargetType.REPOSITORY,
                    targetName: "npm-local",
                    filters: [],
                    artifactoryId: "default",
            )

            XrayWatchModel xrayWatch = new XrayWatchModel()
            xrayWatch.with {
                name = "automation-watch"
                description = "Watching a simple Local repository"
                resources = [resource]
                policies = [policy1, policy2]
                active = true
            }

            try {
                client.v2().policy().create(policy1)
                println("$policy1.name created successfully")
            }
            catch (XrayRestClientException erce){
                println("$policy1.name Was not create, caugth an XrayRestClientException Exception: \n $erce")
            }

            try {
                client.v2().policy().create(policy2)
                println("$policy2.name created successfully")
            }
            catch (XrayRestClientException erce){
                println("$policy1.name Was not create, caugth an XrayRestClientException Exception: \n $erce")
            }
            try {
                client.v2().watch().create(xrayWatch)
                println("Watch $xrayWatch.name created successfully")
            }
            catch (XrayRestClientException erce){
                println("Caugth an Exception: \n $erce")
            }
        }
        else {
            println("Xray did not respond with a pong.")
        }
    }
}