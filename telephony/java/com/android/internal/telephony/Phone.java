/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import com.android.internal.telephony.gsm.NetworkInfo;
import com.android.internal.telephony.gsm.PdpConnection;
import com.android.internal.telephony.test.SimulatedRadioControl;

import java.util.List;

/**
 * Internal interface used to control the phone; SDK developers cannot
 * obtain this interface.
 *
 * {@hide}
 *
 */
public interface Phone {

    /** used to enable additional debug messages */
    static final boolean DEBUG_PHONE = true;
    

    /** 
     * The phone state. One of the following:<p>
     * <ul>
     * <li>IDLE = no phone activity</li>
     * <li>RINGING = a phone call is ringing or call waiting. 
     *  In the latter case, another call is active as well</li>
     * <li>OFFHOOK = The phone is off hook. At least one call
     * exists that is dialing, active or holding and no calls are
     * ringing or waiting.</li>
     * </ul>
     */
    enum State {
        IDLE, RINGING, OFFHOOK;
    };

    /**
     * The state of a data connection.
     * <ul>
     * <li>CONNECTED = IP traffic should be available</li>
     * <li>CONNECTING = Currently setting up data connection</li>
     * <li>DISCONNECTED = IP not available</li>
     * <li>SUSPENDED = connection is created but IP traffic is
     *                 temperately not available. i.e. voice call is in place
     *                 in 2G network</li>
     * </ul>
     */
    enum DataState {
        CONNECTED, CONNECTING, DISCONNECTED, SUSPENDED;
    };

    enum DataActivityState {
        /**
         * The state of a data activity.
         * <ul>
         * <li>NONE = No traffic</li>
         * <li>DATAIN = Receiving IP ppp traffic</li>
         * <li>DATAOUT = Sending IP ppp traffic</li>
         * <li>DATAINANDOUT = Both receiving and sending IP ppp traffic</li>
         * </ul>
         */
        NONE, DATAIN, DATAOUT, DATAINANDOUT;
    };

    enum SuppService {
      UNKNOWN, SWITCH, SEPARATE, TRANSFER, CONFERENCE, REJECT, HANGUP;
    };

    static final String STATE_KEY = "state";
    static final String PHONE_NAME_KEY = "phoneName";
    static final String FAILURE_REASON_KEY = "reason";
    static final String STATE_CHANGE_REASON_KEY = "reason";
    static final String DATA_APN_TYPE_KEY = "apnType";
    static final String DATA_APN_KEY = "apn";
    static final String DATA_IFACE_NAME_KEY = "iface";
    static final String NETWORK_UNAVAILABLE_KEY = "networkUnvailable";

    /**
     * APN types for data connections.  These are usage categories for an APN
     * entry.  One APN entry may support multiple APN types, eg, a single APN
     * may service regular internet traffic ("default") as well as MMS-specific
     * connections.<br/>
     * APN_TYPE_ALL is a special type to indicate that this APN entry can
     * service all data connections.
     */
    static final String APN_TYPE_ALL = "*";
    /** APN type for default data traffic */
    static final String APN_TYPE_DEFAULT = "default";
    /** APN type for MMS traffic */
    static final String APN_TYPE_MMS = "mms";

    // "Features" accessible through the connectivity manager
    static final String FEATURE_ENABLE_MMS = "enableMMS";

    /**
     * Return codes for <code>enableApnType()</code>
     */
    static final int APN_ALREADY_ACTIVE     = 0;
    static final int APN_REQUEST_STARTED    = 1;
    static final int APN_TYPE_NOT_AVAILABLE = 2;
    static final int APN_REQUEST_FAILED     = 3;


    /**
     * Optional reasons for disconnect and connect
     */
    static final String REASON_ROAMING_ON = "roamingOn";
    static final String REASON_ROAMING_OFF = "roamingOff";
    static final String REASON_DATA_DISABLED = "dataDisabled";
    static final String REASON_DATA_ENABLED = "dataEnabled";
    static final String REASON_GPRS_ATTACHED = "gprsAttached";
    static final String REASON_GPRS_DETACHED = "gprsDetached";
    static final String REASON_APN_CHANGED = "apnChanged";
    static final String REASON_APN_SWITCHED = "apnSwitched";
    static final String REASON_RESTORE_DEFAULT_APN = "restoreDefaultApn";
    static final String REASON_RADIO_TURNED_OFF = "radioTurnedOff";
    static final String REASON_PDP_RESET = "pdpReset";
    static final String REASON_VOICE_CALL_ENDED = "2GVoiceCallEnded";
    static final String REASON_VOICE_CALL_STARTED = "2GVoiceCallStarted";
    static final String REASON_PS_RESTRICT_ENABLED = "psRestrictEnabled";
    static final String REASON_PS_RESTRICT_DISABLED = "psRestrictDisabled";
    
    // Used for band mode selection methods
    static final int BM_UNSPECIFIED = 0; // selected by baseband automatically
    static final int BM_EURO_BAND   = 1; // GSM-900 / DCS-1800 / WCDMA-IMT-2000
    static final int BM_US_BAND     = 2; // GSM-850 / PCS-1900 / WCDMA-850 / WCDMA-PCS-1900
    static final int BM_JPN_BAND    = 3; // WCDMA-800 / WCDMA-IMT-2000
    static final int BM_AUS_BAND    = 4; // GSM-900 / DCS-1800 / WCDMA-850 / WCDMA-IMT-2000
    static final int BM_AUS2_BAND   = 5; // GSM-900 / DCS-1800 / WCDMA-850
    static final int BM_BOUNDARY    = 6; // upper band boundary

    // Used for preferred network type
    static final int NT_AUTO_TYPE  = 0;  //   WCDMA preferred (auto mode)
    static final int NT_GSM_TYPE   = 1;  //   GSM only
    static final int NT_WCDMA_TYPE = 2;  //   WCDMA only

    /**
     * Get the current ServiceState. Use 
     * <code>registerForServiceStateChanged</code> to be informed of
     * updates.
     */
    ServiceState getServiceState();

    /**
     * Get the current CellLocation.
     */
    CellLocation getCellLocation();
    
    /**
     * Get the current DataState. No change notification exists at this
     * interface -- use 
     * {@link com.android.internal.telephony.PhoneStateIntentReceiver PhoneStateIntentReceiver} instead.
     */
    DataState getDataConnectionState();

    /**
     * Get the current DataActivityState. No change notification exists at this
     * interface -- use
     * {@link TelephonyManager} instead.
     */
    DataActivityState getDataActivityState();
    
    /**
     * Gets the context for the phone, as set at initialization time.
     */
    Context getContext();

    /** 
     * Get current coarse-grained voice call state.
     * Use {@link #registerForPhoneStateChanged(Handler, int, Object) 
     * registerForPhoneStateChanged()} for change notification. <p>
     * If the phone has an active call and call waiting occurs,
     * then the phone state is RINGING not OFFHOOK
     * <strong>Note:</strong> 
     * This registration point provides notification of finer-grained
     * changes.<p>
     *
     */
    State getState();

    /** 
     * Returns a string identifier for this phone interface for parties
     *  outside the phone app process.
     *  @return The string name.
     */
    String getPhoneName();

    /** 
     * Returns an array of string identifiers for the APN types serviced by the
     * currently active or last connected APN.
     *  @return The string array.
     */
    String[] getActiveApnTypes();
    
    /** 
     * Returns a string identifier for currently active or last connected APN.
     *  @return The string name.
     */
    String getActiveApn();
    
    /** 
     * Get current signal strength. No change notification available on this
     * interface. Use <code>PhoneStateNotifier</code> or an equivalent.
     * An ASU is 0-31 or -1 if unknown (for GSM, dBm = -113 - 2 * asu). 
     * The following special values are defined:</p>
     * <ul><li>0 means "-113 dBm or less".</li>
     * <li>31 means "-51 dBm or greater".</li></ul>
     * 
     * @return Current signal strength in ASU's.
     */
    int getSignalStrengthASU();
    
    /** 
     * Notifies when a previously untracked non-ringing/waiting connection has appeared.
     * This is likely due to some other entity (eg, SIM card application) initiating a call.
     */
    void registerForUnknownConnection(Handler h, int what, Object obj);

    /**
     * Unregisters for unknown connection notifications.
     */
    void unregisterForUnknownConnection(Handler h);

    /** 
     * Notifies when any aspect of the voice call state changes.
     * Resulting events will have an AsyncResult in <code>Message.obj</code>.
     * AsyncResult.userData will be set to the obj argument here.
     * The <em>h</em> parameter is held only by a weak reference.
     */
    void registerForPhoneStateChanged(Handler h, int what, Object obj);

    /**
     * Unregisters for voice call state change notifications. 
     * Extraneous calls are tolerated silently.
     */
    void unregisterForPhoneStateChanged(Handler h);


    /** 
     * Notifies when a new ringing or waiting connection has appeared.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = a Connection. <p>
     *  Please check Connection.isRinging() to make sure the Connection
     *  has not dropped since this message was posted.
     *  If Connection.isRinging() is true, then 
     *   Connection.getCall() == Phone.getRingingCall()
     */
    void registerForNewRingingConnection(Handler h, int what, Object obj);

    /**
     * Unregisters for new ringing connection notification. 
     * Extraneous calls are tolerated silently
     */

    void unregisterForNewRingingConnection(Handler h);

    /** 
     * Notifies when an incoming call rings.<p>
     *
     *  Messages received from this:
     *  Message.obj will be an AsyncResult
     *  AsyncResult.userObj = obj
     *  AsyncResult.result = a Connection. <p>
     */
    void registerForIncomingRing(Handler h, int what, Object obj);
    
    /**
     * Unregisters for ring notification. 
     * Extraneous calls are tolerated silently
     */
    
    void unregisterForIncomingRing(Handler h);
    
    
    /** 
     * Notifies when a voice connection has disconnected, either due to local
     * or remote hangup or error.
     * 
     *  Messages received from this will have the following members:<p>
     *  <ul><li>Message.obj will be an AsyncResult</li>
     *  <li>AsyncResult.userObj = obj</li>
     *  <li>AsyncResult.result = a Connection object that is 
     *  no longer connected.</li></ul>
     */
    void registerForDisconnect(Handler h, int what, Object obj);

    /**
     * Unregisters for voice disconnection notification. 
     * Extraneous calls are tolerated silently
     */
    void unregisterForDisconnect(Handler h);


    /**
     * Register for notifications of initiation of a new MMI code request.
     * MMI codes for GSM are discussed in 3GPP TS 22.030.<p>
     *
     * Example: If Phone.dial is called with "*#31#", then the app will
     * be notified here.<p>
     *
     * The returned <code>Message.obj</code> will contain an AsyncResult.
     *
     * <code>obj.result</code> will be an "MmiCode" object.
     */
    void registerForMmiInitiate(Handler h, int what, Object obj);

    /**
     * Unregisters for new MMI initiate notification. 
     * Extraneous calls are tolerated silently
     */
    void unregisterForMmiInitiate(Handler h);

    /**
     * Register for notifications that an MMI request has completed
     * its network activity and is in its final state. This may mean a state
     * of COMPLETE, FAILED, or CANCELLED.
     *
     * <code>Message.obj</code> will contain an AsyncResult.
     * <code>obj.result</code> will be an "MmiCode" object
     */
    void registerForMmiComplete(Handler h, int what, Object obj);

    /**
     * Unregisters for MMI complete notification. 
     * Extraneous calls are tolerated silently
     */
    void unregisterForMmiComplete(Handler h);

    /**
     * Returns a list of MMI codes that are pending. (They have initiated
     * but have not yet completed).
     * Presently there is only ever one.
     * Use <code>registerForMmiInitiate</code> 
     * and <code>registerForMmiComplete</code> for change notification.
     */
    public List<? extends MmiCode> getPendingMmiCodes();

    /**
     * Sends user response to a USSD REQUEST message.  An MmiCode instance
     * representing this response is sent to handlers registered with
     * registerForMmiInitiate.
     *
     * @param ussdMessge    Message to send in the response.
     */
    public void sendUssdResponse(String ussdMessge);

    /**
     * Register for ServiceState changed. 
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a ServiceState instance
     */
    void registerForServiceStateChanged(Handler h, int what, Object obj);

    /**
     * Unregisters for ServiceStateChange notification. 
     * Extraneous calls are tolerated silently
     */
    void unregisterForServiceStateChanged(Handler h);

    /**
     * Register for Supplementary Service notifications from the network.
     * Message.obj will contain an AsyncResult.
     * AsyncResult.result will be a SuppServiceNotification instance.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    void registerForSuppServiceNotification(Handler h, int what, Object obj);

    /**
     * Unregisters for Supplementary Service notifications. 
     * Extraneous calls are tolerated silently
     * 
     * @param h Handler to be removed from the registrant list.
     */
    void unregisterForSuppServiceNotification(Handler h);

    /**
     * Register for notifications when a supplementary service attempt fails.
     * Message.obj will contain an AsyncResult.
     *
     * @param h Handler that receives the notification message.
     * @param what User-defined message code.
     * @param obj User object.
     */
    void registerForSuppServiceFailed(Handler h, int what, Object obj);

    /**
     * Unregister for notifications when a supplementary service attempt fails.
     * Extraneous calls are tolerated silently
     * 
     * @param h Handler to be removed from the registrant list.
     */
    void unregisterForSuppServiceFailed(Handler h);

    /** 
     * Returns SIM record load state. Use 
     * <code>getSimCard().registerForReady()</code> for change notification.
     *
     * @return true if records from the SIM have been loaded and are 
     * available (if applicable). If not applicable to the underlying
     * technology, returns true as well.
     */
    boolean getSimRecordsLoaded();

    /**
     * Returns the SIM card interface for this phone, or null
     * if not applicable to underlying technology.
     */
    SimCard getSimCard();

    /**
     * Answers a ringing or waiting call. Active calls, if any, go on hold. 
     * Answering occurs asynchronously, and final notification occurs via
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.
     *
     * @exception CallStateException when no call is ringing or waiting
     */
    void acceptCall() throws CallStateException;

    /** 
     * Reject (ignore) a ringing call. In GSM, this means UDUB  
     * (User Determined User Busy). Reject occurs asynchronously, 
     * and final notification occurs via 
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.
     *
     * @exception CallStateException when no call is ringing or waiting
     */
    void rejectCall() throws CallStateException;

    /** 
     * Places any active calls on hold, and makes any held calls
     *  active. Switch occurs asynchronously and may fail.
     * Final notification occurs via 
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.
     *
     * @exception CallStateException if a call is ringing, waiting, or
     * dialing/alerting. In these cases, this operation may not be performed.
     */
    void switchHoldingAndActive() throws CallStateException;

    /**
     * Whether or not the phone can conference in the current phone 
     * state--that is, one call holding and one call active.
     * @return true if the phone can conference; false otherwise. 
     */
    boolean canConference();

    /**
     * Conferences holding and active. Conference occurs asynchronously 
     * and may fail. Final notification occurs via 
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.    
     * 
     * @exception CallStateException if canConference() would return false.
     * In these cases, this operation may not be performed.
     */
    void conference() throws CallStateException;

    /**
     * Whether or not the phone can do explicit call transfer in the current
     * phone state--that is, one call holding and one call active.
     * @return true if the phone can do explicit call transfer; false otherwise.
     */
    boolean canTransfer();

    /**
     * Connects the two calls and disconnects the subscriber from both calls
     * Explicit Call Transfer occurs asynchronously
     * and may fail. Final notification occurs via
     * {@link #registerForPhoneStateChanged(android.os.Handler, int,
     * java.lang.Object) registerForPhoneStateChanged()}.
     *
     * @exception CallStateException if canTransfer() would return false.
     * In these cases, this operation may not be performed.
     */
    void explicitCallTransfer() throws CallStateException;

    /**
     * Clears all DISCONNECTED connections from Call connection lists.
     * Calls that were in the DISCONNECTED state become idle. This occurs
     * synchronously.
     */
    void clearDisconnected();


    /** 
     * Gets the foreground call object, which represents all connections that 
     * are dialing or active (all connections 
     * that have their audio path connected).<p>
     *
     * The foreground call is a singleton object. It is constant for the life
     * of this phone. It is never null.<p>
     * 
     * The foreground call will only ever be in one of these states:
     * IDLE, ACTIVE, DIALING, ALERTING, or DISCONNECTED. 
     *
     * State change notification is available via
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.
     */
    Call getForegroundCall();

    /** 
     * Gets the background call object, which represents all connections that
     * are holding (all connections that have been accepted or connected, but
     * do not have their audio path connected). <p>
     *
     * The background call is a singleton object. It is constant for the life
     * of this phone object . It is never null.<p>
     * 
     * The background call will only ever be in one of these states:
     * IDLE, HOLDING or DISCONNECTED.
     *
     * State change notification is available via
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.
     */
    Call getBackgroundCall();

    /** 
     * Gets the ringing call object, which represents an incoming 
     * connection (if present) that is pending answer/accept. (This connection
     * may be RINGING or WAITING, and there may be only one.)<p>

     * The ringing call is a singleton object. It is constant for the life
     * of this phone. It is never null.<p>
     * 
     * The ringing call will only ever be in one of these states:
     * IDLE, INCOMING, WAITING or DISCONNECTED.
     *
     * State change notification is available via
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}.
     */
    Call getRingingCall();

    /** 
     * Initiate a new voice connection. This happens asynchronously, so you
     * cannot assume the audio path is connected (or a call index has been
     * assigned) until PhoneStateChanged notification has occurred.
     *
     * @exception CallStateException if a new outgoing call is not currently
     * possible because no more call slots exist or a call exists that is 
     * dialing, alerting, ringing, or waiting.  Other errors are 
     * handled asynchronously.
     */
    Connection dial(String dialString) throws CallStateException;

    /**
     * Handles PIN MMI commands (PIN/PIN2/PUK/PUK2), which are initiated
     * without SEND (so <code>dial</code> is not appropriate).
     * 
     * @param dialString the MMI command to be executed.
     * @return true if MMI command is executed.
     */
    boolean handlePinMmi(String dialString);

    /**
     * Handles in-call MMI commands. While in a call, or while receiving a
     * call, use this to execute MMI commands.
     * see 3GPP 20.030, section 6.5.5.1 for specs on the allowed MMI commands.
     *
     * @param command the MMI command to be executed.
     * @return true if the MMI command is executed.
     * @throws CallStateException
     */
    boolean handleInCallMmiCommands(String command) throws CallStateException;

    /**
     * Play a DTMF tone on the active call. Ignored if there is no active call. 
     * @param c should be one of 0-9, '*' or '#'. Other values will be
     * silently ignored.
     */
    void sendDtmf(char c);

    /**
     * Start to paly a DTMF tone on the active call. Ignored if there is no active call
     * or there is a playing DTMF tone.
     * @param c should be one of 0-9, '*' or '#'. Other values will be
     * silently ignored.
     */
    void startDtmf(char c);

    /**
     * Stop the playing DTMF tone. Ignored if there is no playing DTMF
     * tone or no active call.
     */
    void stopDtmf();


    /**
     * Sets the radio power on/off state (off is sometimes 
     * called "airplane mode"). Current state can be gotten via 
     * {@link #getServiceState()}.{@link 
     * android.telephony.ServiceState#getState() getState()}.
     * <strong>Note: </strong>This request is asynchronous. 
     * getServiceState().getState() will not change immediately after this call.
     * registerForServiceStateChanged() to find out when the 
     * request is complete.
     *
     * @param power true means "on", false means "off". 
     */
    void setRadioPower(boolean power);

    /** 
     * Get voice message waiting indicator status. No change notification
     * available on this interface. Use PhoneStateNotifier or similar instead.
     *
     * @return true if there is a voice message waiting
     */
    boolean getMessageWaitingIndicator();

    /**
     * Get voice call forwarding indicator status. No change notification
     * available on this interface. Use PhoneStateNotifier or similar instead.
     *
     * @return true if there is a voice call forwarding
     */
    boolean getCallForwardingIndicator();

    /**
     * Get the line 1 phone number (MSISDN).<p>
     *
     * @return phone number. May return null if not
     * available or the SIM is not ready
     */
    String getLine1Number();

    /**
     * Returns the alpha tag associated with the msisdn number.
     * If there is no alpha tag associated or the record is not yet available,
     * returns a default localized string. <p>
     */
    String getLine1AlphaTag();

    /**
     * Sets the MSISDN phone number in the SIM card.
     *
     * @param alphaTag the alpha tag associated with the MSISDN phone number
     *        (see getMsisdnAlphaTag)
     * @param number the new MSISDN phone number to be set on the SIM.
     * @param onComplete a callback message when the action is completed.
     */
    void setLine1Number(String alphaTag, String number, Message onComplete);

    /**
     * Get the voice mail access phone number. Typically dialed when the 
     * user holds the "1" key in the phone app. May return null if not 
     * available or the SIM is not ready.<p>
     */
    String getVoiceMailNumber();

    /**
     * Returns the alpha tag associated with the voice mail number.
     * If there is no alpha tag associated or the record is not yet available,
     * returns a default localized string. <p>
     * 
     * Please use this value instead of some other localized string when 
     * showing a name for this number in the UI. For example, call log
     * entries should show this alpha tag. <p>
     *
     * Usage of this alpha tag in the UI is a common carrier requirement.
     */
    String getVoiceMailAlphaTag();

    /**
     * setVoiceMailNumber
     * sets the voicemail number in the SIM card.
     *
     * @param alphaTag the alpha tag associated with the voice mail number
     *        (see getVoiceMailAlphaTag)
     * @param voiceMailNumber the new voicemail number to be set on the SIM.
     * @param onComplete a callback message when the action is completed.
     */
    void setVoiceMailNumber(String alphaTag,
                            String voiceMailNumber,
                            Message onComplete);

    /**
     * getCallForwardingOptions
     * gets a call forwarding option. The return value of 
     * ((AsyncResult)onComplete.obj) is an array of CallForwardInfo. 
     * 
     * @param commandInterfaceCFReason is one of the valid call forwarding 
     *        CF_REASONS, as defined in 
     *        <code>com.android.internal.telephony.gsm.CommandsInterface</code>
     * @param onComplete a callback message when the action is completed.
     *        @see com.android.internal.telephony.gsm.CallForwardInfo for details.
     */
    void getCallForwardingOption(int commandInterfaceCFReason,
                                  Message onComplete);
    
    /**
     * setCallForwardingOptions
     * sets a call forwarding option.
     * 
     * @param commandInterfaceCFReason is one of the valid call forwarding 
     *        CF_REASONS, as defined in 
     *        <code>com.android.internal.telephony.gsm.CommandsInterface</code>
     * @param commandInterfaceCFAction is one of the valid call forwarding 
     *        CF_ACTIONS, as defined in 
     *        <code>com.android.internal.telephony.gsm.CommandsInterface</code>
     * @param dialingNumber is the target phone number to forward calls to 
     * @param timerSeconds is used by CFNRy to indicate the timeout before
     *        forwarding is attempted.
     * @param onComplete a callback message when the action is completed.
     */
    void setCallForwardingOption(int commandInterfaceCFReason,
                                 int commandInterfaceCFAction,
                                 String dialingNumber,
                                 int timerSeconds,
                                 Message onComplete);
    
    /**
     * getOutgoingCallerIdDisplay
     * gets outgoing caller id display. The return value of 
     * ((AsyncResult)onComplete.obj) is an array of int, with a length of 2.
     * 
     * @param onComplete a callback message when the action is completed.
     *        @see com.android.internal.telephony.gsm.CommandsInterface.getCLIR for details.
     */
    void getOutgoingCallerIdDisplay(Message onComplete);
    
    /**
     * setOutgoingCallerIdDisplay
     * sets a call forwarding option. 
     * 
     * @param commandInterfaceCLIRMode is one of the valid call CLIR 
     *        modes, as defined in 
     *        <code>com.android.internal.telephony.gsm.CommandsInterface</code>
     * @param onComplete a callback message when the action is completed.
     */
    void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode,
                                    Message onComplete);
    
    /**
     * getCallWaiting
     * gets call waiting activation state. The return value of 
     * ((AsyncResult)onComplete.obj) is an array of int, with a length of 1.
     * 
     * @param onComplete a callback message when the action is completed.
     *        @see com.android.internal.telephony.gsm.CommandsInterface.queryCallWaiting for details.
     */
    void getCallWaiting(Message onComplete);
    
    /**
     * setCallWaiting
     * sets a call forwarding option. 
     * 
     * @param enable is a boolean representing the state that you are 
     *        requesting, true for enabled, false for disabled.
     * @param onComplete a callback message when the action is completed.
     */
    void setCallWaiting(boolean enable, Message onComplete);
    
    /**
     * Scan available networks. This method is asynchronous; .
     * On completion, <code>response.obj</code> is set to an AsyncResult with
     * one of the following members:.<p>
     *<ul>
     * <li><code>response.obj.result</code> will be a <code>List</code> of 
     * <code>com.android.internal.telephony.gsm.NetworkInfo</code> objects, or</li> 
     * <li><code>response.obj.exception</code> will be set with an exception 
     * on failure.</li>
     * </ul>
     */
    void getAvailableNetworks(Message response);    

    /**
     * Switches network selection mode to "automatic", re-scanning and
     * re-selecting a network if appropriate.
     * 
     * @param response The message to dispatch when the network selection 
     * is complete.
     * 
     * @see #selectNetworkManually(com.android.internal.telephony.gsm.NetworkInfo, 
     * android.os.Message )
     */
    void setNetworkSelectionModeAutomatic(Message response);

    /**
     * Manually selects a network. <code>response</code> is 
     * dispatched when this is complete.  <code>response.obj</code> will be
     * an AsyncResult, and <code>response.obj.exception</code> will be non-null
     * on failure.
     * 
     * @see #setNetworkSelectionModeAutomatic(Message)
     */
    void selectNetworkManually(NetworkInfo network, 
                            Message response);

    /**
     *  Requests to set the preferred network type for searching and registering
     * (CS/PS domain, RAT, and operation mode)
     * @param networkType one of  NT_*_TYPE
     * @param response is callback message
     */
    void setPreferredNetworkType(int networkType, Message response);

    /**
     *  Query the preferred network type setting
     *
     * @param response is callback message to report one of  NT_*_TYPE
     */
    void getPreferredNetworkType(Message response);

    /**
     * Query neighboring cell IDs.  <code>response</code> is dispatched when
     * this is complete.  <code>response.obj</code> will be an AsyncResult,
     * and <code>response.obj.exception</code> will be non-null on failure.
     * On success, <code>AsyncResult.result</code> will be a <code>String[]</code>
     * containing the neighboring cell IDs.  Index 0 will contain the count
     * of available cell IDs.  Cell IDs are in hexadecimal format.
     *
     * @param response callback message that is dispatched when the query
     * completes. 
     */
    void getNeighboringCids(Message response);

    /**
     * Sets an event to be fired when the telephony system processes
     * a post-dial character on an outgoing call.<p>
     *
     * Messages of type <code>what</code> will be sent to <code>h</code>.
     * The <code>obj</code> field of these Message's will be instances of
     * <code>AsyncResult</code>. <code>Message.obj.result</code> will be
     * a Connection object.<p>
     *
     * Message.arg1 will be the post dial character being processed, 
     * or 0 ('\0') if end of string.<p>
     *
     * If Connection.getPostDialState() == WAIT, 
     * the application must call 
     * {@link com.android.internal.telephony.Connection#proceedAfterWaitChar() 
     * Connection.proceedAfterWaitChar()} or 
     * {@link com.android.internal.telephony.Connection#cancelPostDial() 
     * Connection.cancelPostDial()}
     * for the telephony system to continue playing the post-dial 
     * DTMF sequence.<p>
     *
     * If Connection.getPostDialState() == WILD, 
     * the application must call 
     * {@link com.android.internal.telephony.Connection#proceedAfterWildChar
     * Connection.proceedAfterWildChar()}
     * or 
     * {@link com.android.internal.telephony.Connection#cancelPostDial() 
     * Connection.cancelPostDial()}
     * for the telephony system to continue playing the 
     * post-dial DTMF sequence.<p>
     * 
     * Only one post dial character handler may be set. <p>
     * Calling this method with "h" equal to null unsets this handler.<p>
     */
    void setOnPostDialCharacter(Handler h, int what, Object obj);


    /**
     * Mutes or unmutes the microphone for the active call. The microphone 
     * is automatically unmuted if a call is answered, dialed, or resumed 
     * from a holding state.
     * 
     * @param muted true to mute the microphone, 
     * false to activate the microphone.
     */

    void setMute(boolean muted);

    /**
     * Gets current mute status. Use 
     * {@link #registerForPhoneStateChanged(android.os.Handler, int, 
     * java.lang.Object) registerForPhoneStateChanged()}
     * as a change notifcation, although presently phone state changed is not
     * fired when setMute() is called.
     *
     * @return true is muting, false is unmuting
     */
    boolean getMute();

    /**
     * Invokes RIL_REQUEST_OEM_HOOK_RAW on RIL implementation.
     * 
     * @param data The data for the request.
     * @param response <strong>On success</strong>, 
     * (byte[])(((AsyncResult)response.obj).result)
     * <strong>On failure</strong>, 
     * (((AsyncResult)response.obj).result) == null and 
     * (((AsyncResult)response.obj).exception) being an instance of
     * com.android.internal.telephony.gsm.CommandException
     *
     * @see #invokeOemRilRequestRaw(byte[], android.os.Message)
     */
    void invokeOemRilRequestRaw(byte[] data, Message response);

    /**
     * Invokes RIL_REQUEST_OEM_HOOK_Strings on RIL implementation.
     * 
     * @param strings The strings to make available as the request data.
     * @param response <strong>On success</strong>, "response" bytes is 
     * made available as:
     * (String[])(((AsyncResult)response.obj).result).
     * <strong>On failure</strong>, 
     * (((AsyncResult)response.obj).result) == null and 
     * (((AsyncResult)response.obj).exception) being an instance of
     * com.android.internal.telephony.gsm.CommandException
     *
     * @see #invokeOemRilRequestStrings(java.lang.String[], android.os.Message)
     */
    void invokeOemRilRequestStrings(String[] strings, Message response);

    /**
     * Get the current active PDP context list
     *
     * @param response <strong>On success</strong>, "response" bytes is
     * made available as:
     * (String[])(((AsyncResult)response.obj).result).
     * <strong>On failure</strong>,
     * (((AsyncResult)response.obj).result) == null and
     * (((AsyncResult)response.obj).exception) being an instance of
     * com.android.internal.telephony.gsm.CommandException
     */
    void getPdpContextList(Message response);

    /**
     * Get current mutiple PDP link status
     * 
     * @return list of pdp link connections
     */
    List<PdpConnection> getCurrentPdpList ();

    /**
     * Udpate LAC and CID in service state for currnet GSM netowrk registration
     *
     * If get different LAC and/or CID, notifyServiceState will be sent
     *
     * @param
     * <strong>On failure</strong>,
     * (((AsyncResult)response.obj).result) == null and
     * (((AsyncResult)response.obj).exception) being an instance of
     * com.android.internal.telephony.gsm.CommandException
     */
    void updateServiceLocation(Message response);

    /**
     * Enable location update notifications.
     */
    void enableLocationUpdates();

    /**
     * Disable location update notifications.
     */
    void disableLocationUpdates();

    /**
     * For unit tests; don't send notifications to "Phone" 
     * mailbox registrants if true.
     */
    void setUnitTestMode(boolean f);
    
    /**
     * @return true If unit test mode is enabled
     */
    boolean getUnitTestMode();

    /**
     * Assign a specified band for RF configuration.
     *
     * @param bandMode one of BM_*_BAND
     * @param response is callback message
     */
    void setBandMode(int bandMode, Message response);

    /**
     * Query the list of band mode supported by RF.
     *
     * @param response is callback message
     *        ((AsyncResult)response.obj).result  is an int[] with every
     *        element representing one avialable BM_*_BAND
     */
    void queryAvailableBandMode(Message response);

    /**
     * @return true if enable data connection on roaming
     */
    boolean getDataRoamingEnabled();

    /**
     * @param enable set true if enable data connection on roaming
     */
    void setDataRoamingEnabled(boolean enable);

    /**
     * If this is a simulated phone interface, returns a SimulatedRadioControl.
     * @ return A SimulatedRadioControl if this is a simulated interface; 
     * otherwise, null.
     */
    SimulatedRadioControl getSimulatedRadioControl();

    /**
     * Allow mobile data connections.
     * @return {@code true} if the operation started successfully
     * <br/>{@code false} if it
     * failed immediately.<br/>
     * Even in the {@code true} case, it may still fail later
     * during setup, in which case an asynchronous indication will
     * be supplied.
     */
    boolean enableDataConnectivity();

    /**
     * Disallow mobile data connections, and terminate any that
     * are in progress.
     * @return {@code true} if the operation started successfully
     * <br/>{@code false} if it
     * failed immediately.<br/>
     * Even in the {@code true} case, it may still fail later
     * during setup, in which case an asynchronous indication will
     * be supplied.
     */
    boolean disableDataConnectivity();

    /**
     * Enables the specified APN type. Only works for "special" APN types,
     * i.e., not the default APN.
     * @param type The desired APN type. Cannot be {@link #APN_TYPE_DEFAULT}.
     * @return <code>APN_ALREADY_ACTIVE</code> if the current APN
     * services the requested type.<br/>
     * <code>APN_TYPE_NOT_AVAILABLE</code> if the carrier does not
     * support the requested APN.<br/>
     * <code>APN_REQUEST_STARTED</code> if the request has been initiated.<br/>
     * <code>APN_REQUEST_FAILED</code> if the request was invalid.<br/>
     * A <code>ACTION_ANY_DATA_CONNECTION_STATE_CHANGED</code> broadcast will
     * indicate connection state progress.
     */
    int enableApnType(String type);

    /**
     * Disables the specified APN type, and switches back to the default APN,
     * if necessary. Switching to the default APN will not happen if default
     * data traffic has been explicitly disabled via a call to {@link #disableDataConnectivity}.
     * <p/>Only works for "special" APN types,
     * i.e., not the default APN.
     * @param type The desired APN type. Cannot be {@link #APN_TYPE_DEFAULT}.
     * @return <code>APN_ALREADY_ACTIVE</code> if the default APN
     * is already active.<br/>
     * <code>APN_REQUEST_STARTED</code> if the request to switch to the default
     * APN has been initiated.<br/>
     * <code>APN_REQUEST_FAILED</code> if the request was invalid.<br/>
     * A <code>ACTION_ANY_DATA_CONNECTION_STATE_CHANGED</code> broadcast will
     * indicate connection state progress.
     */
    int disableApnType(String type);

    /**
     * Report on whether data connectivity is allowed.
     */
    boolean isDataConnectivityPossible();

    /**
     * Returns the name of the network interface used by the specified APN type.
     */
    String getInterfaceName(String apnType);

    /**
     * Returns the IP address of the network interface used by the specified
     * APN type.
     */
    String getIpAddress(String apnType);

    /**
     * Returns the gateway for the network interface used by the specified APN
     * type.
     */
    String getGateway(String apnType);

    /**
     * Returns the DNS servers for the network interface used by the specified
     * APN type.
     */
    public String[] getDnsServers(String apnType);

    /**
     * Retrieves the unique device ID, e.g., IMEI for GSM phones.
     */
    String getDeviceId();

    /**
     * Retrieves the software version number for the device, e.g., IMEI/SV
     * for GSM phones.
     */
    String getDeviceSvn();

    /**
     * Retrieves the unique sbuscriber ID, e.g., IMSI for GSM phones.
     */
    String getSubscriberId();

    /**
     * Retrieves the serial number of the SIM, if applicable.
     */
    String getSimSerialNumber();
}