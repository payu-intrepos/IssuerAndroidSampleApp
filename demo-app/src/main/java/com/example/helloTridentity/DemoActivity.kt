package com.example.helloTridentity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.helloTridentity.adapter.SimAdapter
import com.example.helloTridentity.data.SIMCardApp
import com.example.helloTridentity.databinding.ActivityDemoBinding
import com.google.gson.Gson
import com.wibmo.tridentity.sdk.TridentitySDK
import com.wibmo.tridentity.sdk.theme.BottomSheetAuthenticationPopupConfiguration
import com.wibmo.tridentity.sdk.theme.BottomSheetFailureScreenConfiguration
import com.wibmo.tridentity.sdk.theme.BottomSheetPermissionPopupConfiguration
import com.wibmo.tridentity.sdk.theme.BottomSheetRegistrationSuccessfulPopupConfiguration
import com.wibmo.tridentity.sdk.theme.BottomSheetSimBindingProcessingPopupConfiguration
import com.wibmo.tridentity.sdk.theme.ButtonCustomization
import com.wibmo.tridentity.sdk.theme.ConsentScreenConfiguration
import com.wibmo.tridentity.sdk.theme.DeRegPopupConfiguration
import com.wibmo.tridentity.sdk.theme.HeadingContentCustomization
import com.wibmo.tridentity.sdk.theme.LabelCustomization
import com.wibmo.tridentity.sdk.theme.LogoConfig
import com.wibmo.tridentity.sdk.theme.OfflineOTPScreenConfiguration
import com.wibmo.tridentity.sdk.theme.PrimaryButtonCustomization
import com.wibmo.tridentity.sdk.theme.SecondaryButtonCustomization
import com.wibmo.tridentity.sdk.theme.SimBindingScreenConfiguration
import com.wibmo.tridentity.sdk.theme.SubHeadingCustomization
import com.wibmo.tridentity.sdk.theme.TextCustomization
import com.wibmo.tridentity.sdk.theme.ThemeConfig
import com.wibmo.tridentity.sdk.theme.ToolbarCustomization
import com.wibmo.tridentity.sdk.theme.UICustomization
import com.wibmo.tridentity.sdk.theme.TransactionHistoryScreenConfiguration
import com.wibmo.tridentity.sdk.theme.TncScreenConfiguration
import com.wibmo.tridentity.sdk.utils.ButtonTextTransformType
import com.wibmo.tridentity_headless.HeadlessTridentitySDK
import com.wibmo.tridentity_headless.di.ConfigStatusCallback
import com.wibmo.tridentity_headless.di.DeregisterCallBack
import com.wibmo.tridentity_headless.di.FetchFeaturesCallBack
import com.wibmo.tridentity_headless.di.OfflineTransactionCallback
import com.wibmo.tridentity_headless.di.RegistrationStatusCallBack
import com.wibmo.tridentity_headless.di.SimInfoCallBack
import com.wibmo.tridentity_headless.di.SimSelectorCallBack
import com.wibmo.tridentity_headless.di.TransactionHistoryCallback
import com.wibmo.tridentity_headless.di.UpdateTransactionCallback
import org.json.JSONArray
import org.json.JSONObject

/**
 * Simple demo activity for Tridentity SDK integration.
 * Buttons: Register (configSDK -> initiateRegistration), Check Status (configSDK -> checkRegistrationStatus),
 * Authenticate Transaction (configSDK -> processTransaction). Every configSDK includes full themeConfig.
 */
class DemoActivity : AppCompatActivity(),
    SimAdapter.ClickListener,
    TransactionHistoryCallback, UpdateTransactionCallback, FetchFeaturesCallBack,
    OfflineTransactionCallback,
    RegistrationStatusCallBack {

    private lateinit var binding: ActivityDemoBinding
    private val sdk get() = TridentitySDK.getInstance()
    private val TAG = "TridentityDemo"

    private var selectedSimIndex = -1
    private var selectedSubscriptionId = -1
    private var simCardsList = ArrayList<SIMCardApp>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.app_name)

        handleNotificationIntent(intent)

        binding.btnConfig.setOnClickListener { configSdkOnly() }
        binding.btnRegister.setOnClickListener { register() }
        binding.btnSelectSim.setOnClickListener { selectSim() }
        binding.btnCheckStatus.setOnClickListener { checkStatus() }
        binding.btnTxnHistory.setOnClickListener { transactionHistory() }
        binding.btnShowOfflineOTP.setOnClickListener { showOfflineOTP() }
        binding.btnDeregister.setOnClickListener { deregister() }
    }

    /** Builds config JSON with all base params and full themeConfig (all possible SDK theme options). */
    private fun buildConfigJson(): JSONObject {
        val json = JSONObject()
        json.put("env", "UAT")
        json.put("clientId", "<ADD YOUR CLIENT ID HERE>")
        binding.etMobile.text?.toString()?.takeIf { it.isNotBlank() }?.let { json.put("mobileNumber", it) }
        binding.etCif.text?.toString()?.takeIf { it.isNotBlank() }?.let { cif ->
            json.put("cif", cif)
        }
        json.put("themeConfig", buildThemeConfig())
        json.put("logoConfig", Gson().toJson(buildLogoConfig()))
        return json
    }

    private fun buildLogoConfig(): LogoConfig{
        return LogoConfig(
            defaultLogo = R.drawable.ic_cd_bank,
            roundedLogo = R.drawable.app_icon,
            gifLogo = R.drawable.loading_processor
        )

    }

    /** All possible themeConfig options available in the SDK. */
    private fun buildThemeConfig(): ThemeConfig {
        return ThemeConfig(
            uiCustomization = UICustomization(
                labelCustomization = LabelCustomization(
                    headingCustomization = HeadingContentCustomization(
                        textColor = "#000000",
                        fontSize = 18f,
                        fontName = ""
                    ),
                    subHeadingCustomization = SubHeadingCustomization(
                        textColor = "#000000",
                        fontSize = 14f,
                        fontName = ""
                    )
                ),
                buttonCustomization = ButtonCustomization(
                    primaryButtonCustomization = PrimaryButtonCustomization(
                        enabledBackgroundColor = "#9C27B0",
                        enabledTextColor = "#FFFFFF",
                        disabledBackgroundColor = "#F26522",
                        disabledTextColor = "#FFFFFF"
                    ),
                    secondaryButtonCustomization = SecondaryButtonCustomization(
                        enabledBackgroundColor = "#9C27B0",
                        enabledTextColor = "#FFFFFF",
                        disabledBackgroundColor = "#F26522",
                        disabledTextColor = "#FFFFFF"
                    ),
                    buttonCornerRadius = 50,
                    fontSize = 18f,
                    fontName = "",
                    buttonTextTransform = ButtonTextTransformType.DEFAULT.name
                ),
                toolbarCustomization = ToolbarCustomization(
                    backgroundColor = "#25272C",
                    textColor = "#1be077",
                    fontSize = 10f,
                    fontName = ""
                )
            ),
            textCustomization = TextCustomization(
                consentScreenConfiguration = ConsentScreenConfiguration(
                    consentPopUpRegisterButtonText = "Register",
                    consentPopUpSkipButtonText = "Skip"
                ),
                tncScreenConfiguration = TncScreenConfiguration(
                    tncPopUpOkButtonText = "OK"
                ),

                simBindingScreenConfiguration = SimBindingScreenConfiguration(
                    appBarText = "SIM Binding",
                    topHeaderText = "SIM Binding",
                    topSubHeaderText = "Verify your number",
                    headerTextForContents = "Select SIM",
                    subTextForContents = "An SMS may be sent for verification.",
                    textForFullScreenSubContent = "Full screen sub content",
                    buttonTextForProceed = "Proceed"
                ),

                transactionHistoryScreenConfiguration = TransactionHistoryScreenConfiguration(
                    appBarText = "Transaction History",
                    topHeaderText = "Authentication History",
                    topSubHeaderText = "Your transactions",
                    headerTextForContents = "Transactions",
                    subTextForContents = "List of transactions",
                    bottomBarTransactionsText = "Transactions",
                    bottomBarOfflineOTPText = "Offline OTP"
                ),
                offlineOTPScreenConfiguration = OfflineOTPScreenConfiguration(
                    appBarText = "Offline OTP",
                    topHeaderText = "Offline OTP",
                    topSubHeaderText = "Use this OTP",
                    headerTextForContents = "OTP",
                    subTextForContents = "Copy and use this OTP",
                    copyText = "Copy"
                ),
                bottomSheetAuthenticationPopupConfiguration = BottomSheetAuthenticationPopupConfiguration(
                    topHeaderText = "Authenticate",
                    topSubHeaderText = "Confirm payment",
                    headerTextForContents = "Confirm transaction",
                    subTextForContents = "Swipe to approve.",
                    declineButtonText = "Decline",
                    swipeToPayButtonText = "Swipe to Pay"
                ),
                deRegPopupConfiguration = DeRegPopupConfiguration(
                    topHeaderText = "De-register",
                    topSubHeaderText = "Remove this device",
                    headerTextForContents = "De-register device?",
                    subTextForContents = "You will need to register again.",
                    okButtonText = "OK",
                    cancelButtonText = "Cancel"
                )
            )
        )
    }

    private fun configSdkOnly() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread {
                    showResult("Config: ${event.optString("message", "OK")}")
                    Toast.makeText(this@DemoActivity, R.string.msg_config_success, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /** 1. Register: configSDK -> onSuccess -> initiateRegistration; print onSuccess/onError. */
    private fun register() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread {
                    showResult("Config success -> calling initiateRegistration")
                    Log.d(TAG, "configSDK onSuccess: $event")
                }
                doInitiateRegistration()
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Log.e(TAG, "configSDK onError: $code - $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /** Select SIM: setUpSimSelector -> show SIM dialog -> setSimInfo on Proceed (like App). */
    private fun selectSim() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread { showResult("Config success -> calling setUpSimSelector") }
                doSetUpSimSelector()
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun doSetUpSimSelector() {

      //Code to get available sims in the device
        // then call showSimUI()
    }

    private fun showSimUI(data: JSONObject) {
        if (!data.has("simCard")) {
            showResult("No SIM cards in response")
            return
        }
        val simArray = JSONArray(data.getString("simCard"))
        if (simArray.length() == 0) {
            showResult("No SIM cards available")
            return
        }
        simCardsList.clear()
        for (i in 0 until simArray.length()) {
            val sim = simArray.getJSONObject(i)
            simCardsList.add(
                SIMCardApp(
                    id = sim.getInt("id"),
                    subscriptionId = sim.getInt("subscriptionId"),
                    label = sim.optString("label", "SIM ${i + 1}"),
                    number = sim.optString("number", "")
                )
            )
        }
        if (simCardsList.size == 1) {
            // Single SIM: auto-select and setSimInfo (simIndex=position, subscriptionId from SIM)
            saveSimIndex(0, simCardsList[0].subscriptionId)
        } else {
            // Multiple SIMs: show selection dialog
            selectedSimIndex = -1
            selectedSubscriptionId = -1
            showSimSelectionDialog()
        }
    }

    private fun showSimSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sim_select, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val simRecycler = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.simRecycler)
        simRecycler.layoutManager = LinearLayoutManager(this)
        simRecycler.adapter = SimAdapter(simCardsList, this)

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnProceed).setOnClickListener {
            if (selectedSimIndex >= 0 && selectedSubscriptionId >= 0) {
                saveSimIndex(selectedSimIndex, selectedSubscriptionId)  // simIndex=position, subscriptionId
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please select a SIM", Toast.LENGTH_SHORT).show()
            }
        }
        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun mOnitemClickListener(position: Int, subsId: Int) {
        selectedSimIndex = position
        selectedSubscriptionId = subsId
    }

    private fun saveSimIndex(simIndex: Int, subscriptionId: Int) {
        val jsonObject = JSONObject()
        jsonObject.put("subscriptionId", subscriptionId)
        jsonObject.put("simIndex", simIndex)
    /******
     *
     * SDK API CALL to SET SIM INFO
     *
     * *******/
        sdk.setSimInfo(
            this,
            jsonObject)
    }

    private fun doInitiateRegistration() {
        val json = JSONObject()
        json.put("fcmToken", "")
        json.put("mobileNumber", binding.etMobile.text?.toString() ?: "")
        binding.etCif.text?.toString()?.takeIf { it.isNotBlank() }?.let { json.put("cif", it) }
        sdk.initiateRegistration(this, json, object : RegistrationStatusCallBack {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread {
                    showResult("Register onSuccess: $event")
                    Log.d(TAG, "initiateRegistration onSuccess: $event")
                    Toast.makeText(this@DemoActivity, R.string.msg_register_success, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onSuccess(event: String) {
                runOnUiThread {
                    showResult("Register onSuccess: $event")
                    Log.d(TAG, "initiateRegistration onSuccess (String): $event")
                }
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Register onError [$code]: $error")
                    Log.e(TAG, "initiateRegistration onError: $code - $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_register_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /** 2. Check Registration Status: configSDK -> onSuccess -> checkRegistrationStatus. */
    private fun checkStatus() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread { showResult("Config success -> calling checkRegistrationStatus") }
                Log.d(TAG, "configSDK onSuccess: $event")
                doCheckRegistrationStatus()
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Log.e(TAG, "configSDK onError: $code - $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun doCheckRegistrationStatus() {
        val json = JSONObject()
        json.put("env", "UAT")
        json.put("clientId", "<ADD YOUR CLIENT ID HERE>")
        binding.etMobile.text?.toString()?.takeIf { it.isNotBlank() }?.let { json.put("mobileNumber", it) }
        binding.etCif.text?.toString()?.takeIf { it.isNotBlank() }?.let { json.put("cif", it) }
        sdk.checkRegistrationStatus(this, json, object : RegistrationStatusCallBack {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread {
                    showResult("CheckStatus onSuccess: $event")
                    Log.d(TAG, "checkRegistrationStatus onSuccess: $event")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_status_success, event.toString()), Toast.LENGTH_SHORT).show()
                }
            }
            override fun onSuccess(event: String) {
                runOnUiThread {
                    showResult("CheckStatus onSuccess: $event")
                    Log.d(TAG, "checkRegistrationStatus onSuccess (String): $event")
                }
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("CheckStatus onError [$code]: $error")
                    Log.e(TAG, "checkRegistrationStatus onError: $code - $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_status_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun transactionHistory() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread { showResult("Config success -> calling getTransactionHistory") }
                doTransactionHistory()
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /** 6. Show Offline OTP: configSDK -> onSuccess -> showOfflineOTP (navigates to Offline OTP screen). */
    private fun showOfflineOTP() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread { showResult("Config success -> calling showOfflineOTP") }
                val json = JSONObject()
                binding.etCif.text?.toString()?.takeIf { it.isNotBlank() }?.let { json.put("customerId", it) }
                sdk.showOfflineOTP(this@DemoActivity, json, this@DemoActivity)
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /** 7. Deregister: configSDK -> onSuccess -> deRegistration (removes device registration). */
    private fun deregister() {
        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                runOnUiThread { showResult("Config success -> calling deRegistration") }
                Log.d(TAG, "configSDK onSuccess -> deRegistration")
                sdk.deRegistration(this@DemoActivity, object : DeregisterCallBack {
                    override fun onSuccess(event: JSONObject) {
                        runOnUiThread {
                            showResult("Deregister onSuccess: ${event.optString("message", event.toString())}")
                            Log.d(TAG, "deRegistration onSuccess: $event")
                            Toast.makeText(this@DemoActivity, R.string.msg_deregister_success, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onSuccess(event: String) {
                        runOnUiThread {
                            showResult("Deregister onSuccess: $event")
                            Log.d(TAG, "deRegistration onSuccess (String): $event")
                            Toast.makeText(this@DemoActivity, R.string.msg_deregister_success, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onError(error: String) {
                        runOnUiThread {
                            showResult("Deregister onError: $error")
                            Log.e(TAG, "deRegistration onError: $error")
                            Toast.makeText(this@DemoActivity, getString(R.string.msg_deregister_error, error), Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onError(code: Int, error: String) {
                        runOnUiThread {
                            showResult("Deregister onError [$code]: $error")
                            Log.e(TAG, "deRegistration onError: $code - $error")
                            Toast.makeText(this@DemoActivity, getString(R.string.msg_deregister_error, error), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error [$code]: $error")
                    Log.e(TAG, "configSDK onError: $code - $error")
                    Toast.makeText(this@DemoActivity, getString(R.string.msg_config_error, error), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun doTransactionHistory() {
        val json = JSONObject()
        binding.etCif.text?.toString()?.takeIf { it.isNotBlank() }?.let { json.put("customerId", it) }
        sdk.getTransactionHistory(this, json, this)
    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    /**
     * Handle push notification data when user taps notification (launches DemoActivity with extras).
     * Flow: configSdk -> checkRegistrationStatus -> processTransaction (like HomeActivity).
     */
    private fun handleNotificationIntent(intent: Intent?) {
        val txnId = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_TXN_ID)
        if (txnId.isNullOrBlank()) return

        val dataMap = mutableMapOf<String, String>()
        dataMap["action"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_ACTION) ?: ""
        dataMap["amount"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_AMOUNT) ?: ""
        dataMap["detail"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_DETAIL) ?: ""
        dataMap["txnTime"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_TXN_TIME) ?: ""
        dataMap["expireTime"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_EXPIRE_TIME) ?: ""
        dataMap["title"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_TITLE) ?: ""
        dataMap["merchantName"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_MERCHANT_NAME) ?: ""
        dataMap["authType"] = intent?.getStringExtra(DemoFirebaseMessagingService.EXTRA_AUTH_TYPE) ?: ""
        dataMap["txnId"] = txnId

        showResult("Push: $txnId - ${dataMap["merchantName"]} ${dataMap["amount"]}")

        sdk.configSdk(this, buildConfigJson(), object : ConfigStatusCallback {
            override fun onSuccess(event: JSONObject) {
                Log.d(TAG, "handleNotificationIntent configSdk onSuccess")
                sdk.checkRegistrationStatus(
                    applicationContext,
                    buildConfigJson(),
                    object : RegistrationStatusCallBack {
                        override fun onSuccess(event: JSONObject) {
                            Log.d(TAG, "handleNotificationIntent checkRegistrationStatus onSuccess")
                            sdk.processTransaction(
                                applicationContext,
                                dataMap,
                                object : UpdateTransactionCallback {
                                    override fun onNotificationClick(remoteMessage: Map<String, String>) {
                                        Log.d(TAG, "onNotificationClick")
                                    }
                                    override fun onSuccess(event: JSONObject) {
                                        runOnUiThread {
                                            showResult("Transaction: ${event.optString("message", "OK")}")
                                            Toast.makeText(this@DemoActivity, R.string.msg_txn_success, Toast.LENGTH_SHORT).show()
                                            dataMap.clear()
                                            intent?.replaceExtras(Bundle())
                                        }
                                    }
                                    override fun onSuccess(event: String) {
                                        runOnUiThread {
                                            showResult("Transaction: $event")
                                            dataMap.clear()
                                            intent?.replaceExtras(Bundle())
                                        }
                                    }
                                    override fun onError(error: String) {
                                        runOnUiThread {
                                            showResult("Transaction error: $error")
                                            Toast.makeText(this@DemoActivity, getString(R.string.msg_txn_error, error), Toast.LENGTH_SHORT).show()
                                            dataMap.clear()
                                            intent?.replaceExtras(Bundle())
                                        }
                                    }
                                },
                                false
                            )
                        }
                        override fun onSuccess(event: String) {
                            runOnUiThread { showResult("CheckStatus: $event")
                                dataMap.clear()
                                intent?.replaceExtras(Bundle())
                            }
                        }
                        override fun onError(code: Int, error: String) {
                            runOnUiThread {
                                showResult("CheckStatus error: $error")
                                Log.e(TAG, "checkRegistrationStatus onError: $code - $error")
                                dataMap.clear()
                                intent?.replaceExtras(Bundle())
                            }
                        }
                    }
                )
            }
            override fun onError(code: Int, error: String) {
                runOnUiThread {
                    showResult("Config error: $error")
                    Log.e(TAG, "handleNotificationIntent configSdk onError: $code - $error")
                    dataMap.clear()
                    intent?.replaceExtras(Bundle())
                }
            }
        })
    }

    private fun showResult(text: String) {
        runOnUiThread {
            binding.tvLog.visibility = android.view.View.VISIBLE
            binding.tvLog.text = text
            binding.root.post {
                binding.root.fullScroll(android.view.View.FOCUS_DOWN)
            }
        }
    }
}
