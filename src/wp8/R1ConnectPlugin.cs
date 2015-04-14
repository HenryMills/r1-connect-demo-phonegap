using Microsoft.Phone.Controls;
using Microsoft.Phone.Notification;
using System;
using System.Diagnostics;
using System.Text;
using System.Threading;
using System.Runtime.Serialization;
using System.Windows;
using Radiumone.Connect;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace WPCordovaClassLib.Cordova.Commands
{
    public class R1ConnectPlugin : BaseCommand
    {
        private class R1ConnectPluginObserver
        {
            private static bool _Initialized = false;
            public void Initialize()
            {
                if (_Initialized)
                    return;

                _Initialized = true;

                System.Windows.Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    var appConfig = new WPCordovaClassLib.CordovaLib.ConfigHandler();
                    appConfig.LoadAppPackageConfig();

                    var r1Config = new Config();

                    r1Config.ApplicationId = appConfig.GetPreference("com.radiumone.r1connect.applicationId");
                    r1Config.ClientKey = appConfig.GetPreference("com.radiumone.r1connect.clientKey");
                    r1Config.MPNSServiceName = appConfig.GetPreference("com.radiumone.r1connect.MPNSServiceName");
                    r1Config.MPNSChannelName = appConfig.GetPreference("com.radiumone.r1connect.MPNSChannelName");

                    var allowedDomains = appConfig.GetPreference("com.radiumone.r1connect.TileAllowedDomains");
                    foreach (var allowedDomain in allowedDomains.Split(','))
                    {
                        try
                        {
                            r1Config.TileAllowedDomains.Add(new Uri(allowedDomain.Trim()));
                        }
                        catch (Exception)
                        {
                        }
                    }

#if DEBUG
                    r1Config.InProduction = false;
#else
            r1Config.InProduction = true;
#endif

                    var disableAllAdvertisingIds = appConfig.GetPreference("com.radiumone.r1connect.disableAllAdvertisingIds");
                    if (disableAllAdvertisingIds != null)
                    {
                        disableAllAdvertisingIds = disableAllAdvertisingIds.ToLower();
                        SDK.Instance.DisableAllAdvertisingIds = (disableAllAdvertisingIds == "true") || (disableAllAdvertisingIds == "yes");
                    }else
                        SDK.Instance.DisableAllAdvertisingIds = false;

                    SDK.Instance.Start(r1Config);

                    RegisterObservers();
                });
            }

            private void RegisterObservers()
            {
                SDK.Instance.LocationService.StateUpdated += LocationService_StateUpdated;
                SDK.Instance.LocationService.LastCoordinateUpdated += LocationService_LastCoordinateUpdated;

                Push.Instance.ToastChannelUpdated += Instance_ToastChannelUpdated;
                Push.Instance.ToastPushReceived += Instance_ToastPushReceived;
                Push.Instance.HttpNotificationReceived += Instance_HttpNotificationReceived;
            }

            void Instance_HttpNotificationReceived(object sender, HttpNotificationEventArgs e)
            {
                string message = "";

                try
                {
                    using (System.IO.StreamReader reader = new System.IO.StreamReader(e.Notification.Body))
                    {
                        message = reader.ReadToEnd();
                    }
                }
                catch (Exception)
                {
                }

                var dict = new Dictionary<string, object>();

                dict.Add("message", message);

                var headers = new Dictionary<string, string>();
                foreach (var key in e.Notification.Headers.AllKeys)
                {
                    headers.Add(key, e.Notification.Headers[key]);
                }

                dict.Add("headers", headers);

                var js = JsonConvert.SerializeObject(dict);

                FireDocumentEvent("'R1Push.foregroundHttpNotification', " + js);
            }

            void Instance_ToastPushReceived(object sender, Radiumone.Connect.PushInfo.ToastPushReceivedEventArgs e)
            {
                var js = JsonConvert.SerializeObject(e.Collection);

                FireDocumentEvent("'R1Push.foregroundNotification', " + js);
            }

            void Instance_ToastChannelUpdated(object sender, Radiumone.Connect.PushInfo.ToastChannelUpdatedEventArgs e)
            {
                FireDocumentEvent("'R1Push.deviceToken', {deviceToken:'" + e.ChannelUri.ToString() + "'}");
            }

            void LocationService_LastCoordinateUpdated(object sender, LocationService.LastLocationUpdatedEventArgs e)
            {
                var newLocation = new Location() { Latitude = e.LastCoordinate.Latitude, Longitude = e.LastCoordinate.Longitude };

                var js = JSON.JsonHelper.Serialize(newLocation);

                FireDocumentEvent("'R1LocationService.coordinate', " + js);
            }

            void LocationService_StateUpdated(object sender, LocationService.StateUpdatedEventArgs e)
            {
                FireDocumentEvent("'R1LocationService.state', {state:'"+LocationStateToString(e.State)+"'}");
            }

            private void FireDocumentEvent(string parameters)
            {
                Deployment.Current.Dispatcher.BeginInvoke(() =>
                {
                    PhoneApplicationFrame frame = Application.Current.RootVisual as PhoneApplicationFrame;
                    if (frame != null)
                    {
                        PhoneApplicationPage page = frame.Content as PhoneApplicationPage;
                        if (page != null)
                        {
                            CordovaView cView = page.FindName("CordovaView") as CordovaView; // was: PGView
                            if (cView != null)
                            {
                                cView.Browser.Dispatcher.BeginInvoke((ThreadStart)delegate()
                                {
                                    try
                                    {
                                        var js = "cordova.fireDocumentEvent(" + parameters + ");";
                                        cView.Browser.InvokeScript("eval", js);
                                    }
                                    catch (Exception ex)
                                    {
                                        Debug.WriteLine("ERROR: Exception in InvokeScriptCallback :: " + ex.Message);
                                    }

                                });
                            }
                        }
                    }
                });
            }
        }

        private static R1ConnectPluginObserver sharedObserver = new R1ConnectPluginObserver();

        public R1ConnectPlugin()
        {
            sharedObserver.Initialize();
        }

        public void isStarted(string json)
        {
            ExecuteAndWait(() =>
            {
                var started = Push.Instance.Started || Emitter.Instance.Started;

                DispathOkResult(started);
            });
        }

        public void getApplicationUserId(string json)
        {
            ExecuteAndWait(() =>
            {
                if (SDK.Instance.ApplicationUserId == null)
                    DispathOkResult("");
                else
                    DispathOkResult(SDK.Instance.ApplicationUserId);
            });
        }

        public void setApplicationUserId(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var newApplicationUserId = args[0];

            ExecuteAndWait(() =>
            {
                SDK.Instance.ApplicationUserId = newApplicationUserId;

                DispathOkResult();
            });
        }

        public void isGeofencingEnabled(string json)
        {
            ExecuteAndWait(() =>
            {
                // TBD: Now R1Connect SDK for WP doesn't have this property
                DispathOkResult(false);
            });
        }

        public void setGeofencingEnabled(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            bool newSetEnabled = false;
            if (!bool.TryParse(args[0], out newSetEnabled))
            {
                DispathWrongParametersResult();
                return;
            }

            ExecuteAndWait(() =>
            {
                // TBD: Now R1Connect SDK for WP doesn't have this property
                DispathOkResult();
            });
        }

        public void isEngageEnabled(string json)
        {
            ExecuteAndWait(() =>
            {
                // TBD: Now R1Connect SDK for WP doesn't have this property
                DispathOkResult(false);
            });
        }

        public void setEngageEnabled(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            bool newSetEnabled = false;
            if (!bool.TryParse(args[0], out newSetEnabled))
            {
                DispathWrongParametersResult();
                return;
            }

            ExecuteAndWait(() =>
            {
                // TBD: Now R1Connect SDK for WP doesn't have this property
                DispathOkResult();
            });
        }

        public void location_isEnabled(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(SDK.Instance.LocationService.Enabled);
            });
        }

        public void location_setEnabled(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            bool newSetEnabled = false;
            if (!bool.TryParse(args[0], out newSetEnabled))
            {
                DispathWrongParametersResult();
                return;
            }

            ExecuteAndWait(() =>
            {
                SDK.Instance.LocationService.Enabled = newSetEnabled;

                DispathOkResult();
            });
        }

        public void location_getAutoupdateTimeout(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(SDK.Instance.LocationService.AutoupdateTimeout.ToString());
            });
        }

        public void location_setAutoupdateTimeout(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            float timeout = 0;
            if (!float.TryParse(args[0], out timeout))
            {
                DispathWrongParametersResult();
                return;
            }

            ExecuteAndWait(() =>
            {
                SDK.Instance.LocationService.AutoupdateTimeout = timeout;

                DispathOkResult();
            });
        }

        public void location_getState(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(LocationStateToString(SDK.Instance.LocationService.State));
            });
        }

        public void location_getCoordinate(string json)
        {
            ExecuteAndWait(() =>
            {
                if (SDK.Instance.LastCoordinate == null)
                {
                    DispathOkResult();
                    return;
                }

                DispathOkResult(new Location() { Latitude = SDK.Instance.LastCoordinate.Latitude, Longitude = SDK.Instance.LastCoordinate.Longitude });
            });
        }

        public void location_updateNow(string json)
        {
            ExecuteAndWait(() =>
            {
                SDK.Instance.LocationService.UpdateNow();

                DispathOkResult();
            });
        }

        protected static string LocationStateToString(LocationService.ServiceState state)
        {
            switch (state)
            {
                case LocationService.ServiceState.Disabled:
                    return "Disabled";
                case LocationService.ServiceState.Off:
                    return "Off";
                case LocationService.ServiceState.Searching:
                    return "Searching";
                case LocationService.ServiceState.WaitNextUpdate:
                    return "Wait Next Update";
            
                default:
                    break;
            }
    
            return "Unknown";
        }

        public void emitter_isStarted(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(Emitter.Instance.Started);
            });
        }

        public void emitter_getAppName(string json)
        {
            ExecuteAndWait(() =>
            {
                if (Emitter.Instance.ApplicationName == null)
                {
                    DispathOkResult("");
                    return;
                }

                DispathOkResult(Emitter.Instance.ApplicationName);
            });
        }

        public void emitter_setAppName(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var newAppName = args[0];

            ExecuteAndWait(() =>
            {
                Emitter.Instance.ApplicationName = newAppName;

                DispathOkResult();
            });
        }

        public void emitter_getAppId(string json)
        {
            ExecuteAndWait(() =>
            {
                if (Emitter.Instance.ApplicationId == null)
                {
                    DispathOkResult("");
                    return;
                }

                DispathOkResult(Emitter.Instance.ApplicationId);
            });
        }

        public void emitter_setAppId(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var newAppId = args[0];

            ExecuteAndWait(() =>
            {
                Emitter.Instance.ApplicationId = newAppId;

                DispathOkResult();
            });
        }

        public void emitter_getAppVersion(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(Emitter.Instance.AppVersion);
            });
        }

        public void emitter_setAppVersion(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var newAppVersion = args[0];

            ExecuteAndWait(() =>
            {
                Emitter.Instance.AppVersion = newAppVersion;

                DispathOkResult();
            });
        }

        public void emitter_getSessionTimeout(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(Emitter.Instance.SessionTimeout.ToString());
            });
        }

        public void emitter_setSessionTimeout(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            float timeout = 0;
            if (!float.TryParse(args[0], out timeout))
            {
                DispathWrongParametersResult();
                return;
            }

            ExecuteAndWait(() =>
            {
                Emitter.Instance.SessionTimeout = timeout;

                DispathOkResult();
            });
        }

        public void emitter_emitEvent(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 3)
            {
                DispathWrongParametersResult();
                return;
            }

            string eventName = args[0];

            var otherInfo = OtherInfoFromJSON(args[1]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitEvent(eventName, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitUserInfo(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 3)
            {
                DispathWrongParametersResult();
                return;
            }

            var jsonUserInfo = JSON.JsonHelper.Deserialize<UserInfo>(args[0]);
            var userInfo = new Radiumone.Connect.EmitterInfo.UserInfo() { UserID = jsonUserInfo.UserID, City = jsonUserInfo.City, Email = jsonUserInfo.Email, FirstName = jsonUserInfo.FirstName, LastName = jsonUserInfo.LastName, Phone = jsonUserInfo.Phone, State = jsonUserInfo.State, StreetAddress = jsonUserInfo.StreetAddress, UserName = jsonUserInfo.UserName, Zip = jsonUserInfo.Zip };

            var otherInfo = OtherInfoFromJSON(args[1]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitUserInfo(userInfo, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitLogin(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 4)
            {
                DispathWrongParametersResult();
                return;
            }

            string userId = args[0];
            string userName = args[1];

            var otherInfo = OtherInfoFromJSON(args[2]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitLogin(userId, userName, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitRegistration(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 7)
            {
                DispathWrongParametersResult();
                return;
            }

            string userId = args[0];
            string userName = args[1];
            string country = args[2];
            string state = args[3];
            string city = args[4];

            var otherInfo = OtherInfoFromJSON(args[5]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitRegistration(userId, userName, country, state, city, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitFBConnect(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 3)
            {
                DispathWrongParametersResult();
                return;
            }

            var jsonPermissions = JSON.JsonHelper.Deserialize<SocialPermission[]>(args[0]);
            var permissions = new List<Radiumone.Connect.EmitterInfo.SocialPermission>();

            foreach (var permission in jsonPermissions)
            {
                permissions.Add(new Radiumone.Connect.EmitterInfo.SocialPermission() { Name = permission.Name, Granted = permission.Granted });
            }

            var otherInfo = OtherInfoFromJSON(args[1]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitFBConnect(permissions, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitTConnect(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 5)
            {
                DispathWrongParametersResult();
                return;
            }

            string userId = args[0];
            string userName = args[1];
            var jsonPermissions = JSON.JsonHelper.Deserialize<SocialPermission[]>(args[2]);
            var permissions = new List<Radiumone.Connect.EmitterInfo.SocialPermission>();

            foreach (var permission in jsonPermissions)
            {
                permissions.Add(new Radiumone.Connect.EmitterInfo.SocialPermission() { Name = permission.Name, Granted = permission.Granted });
            }

            var otherInfo = OtherInfoFromJSON(args[3]);

            ExecuteAndWait(() =>
            {
               Emitter.Instance.EmitTConnect(userId, userName, permissions, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitTransaction(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 11)
            {
                DispathWrongParametersResult();
                return;
            }

            string transactionID = args[0];
            string storeID = args[1];
            string storeName = args[2];
            string cartID = args[3];
            string orderID = args[4];
            double totalSale = 0;
            double.TryParse(args[5], out totalSale);
            string currency = args[6];
            double shippingCosts = 0;
            double.TryParse(args[7], out shippingCosts);
            double transactionTax = 0;
            double.TryParse(args[8], out transactionTax);

            var otherInfo = OtherInfoFromJSON(args[9]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitTransaction(transactionID, storeID, storeName, cartID, orderID, totalSale, currency, shippingCosts, transactionTax, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitTransactionItem(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 4)
            {
                DispathWrongParametersResult();
                return;
            }

            string transactionID = args[0];

            var jsonLineItem = JSON.JsonHelper.Deserialize<LineItem>(args[1]);
            var lineItem = new Radiumone.Connect.EmitterInfo.LineItem() { ProductID = jsonLineItem.UniversalID, ProductName = jsonLineItem.UniversalName, Currency = jsonLineItem.Currency, ItemCategory = jsonLineItem.ItemCategory, MsrPrice = jsonLineItem.MsrPrice, PricePaid = jsonLineItem.PricePaid, Quantity = jsonLineItem.Quantity, UnitOfMeasure = jsonLineItem.UnitOfMeasure};

            var otherInfo = OtherInfoFromJSON(args[2]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitTransactionItem(transactionID, lineItem, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitCartCreate(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 3)
            {
                DispathWrongParametersResult();
                return;
            }

            string cartID = args[0];

            var otherInfo = OtherInfoFromJSON(args[1]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitCartCreate(cartID, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitCartDelete(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 3)
            {
                DispathWrongParametersResult();
                return;
            }

            string cartID = args[0];

            var otherInfo = OtherInfoFromJSON(args[1]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitCartDelete(cartID, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitAddToCart(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 4)
            {
                DispathWrongParametersResult();
                return;
            }

            string cartID = args[0];

            var jsonLineItem = JSON.JsonHelper.Deserialize<LineItem>(args[1]);
            var lineItem = new Radiumone.Connect.EmitterInfo.LineItem() { ProductID = jsonLineItem.UniversalID, ProductName = jsonLineItem.UniversalName, Currency = jsonLineItem.Currency, ItemCategory = jsonLineItem.ItemCategory, MsrPrice = jsonLineItem.MsrPrice, PricePaid = jsonLineItem.PricePaid, Quantity = jsonLineItem.Quantity, UnitOfMeasure = jsonLineItem.UnitOfMeasure };

            var otherInfo = OtherInfoFromJSON(args[2]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitAddToCart(cartID, lineItem, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitDeleteFromCart(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 4)
            {
                DispathWrongParametersResult();
                return;
            }

            string cartID = args[0];

            var jsonLineItem = JSON.JsonHelper.Deserialize<LineItem>(args[1]);
            var lineItem = new Radiumone.Connect.EmitterInfo.LineItem() { ProductID = jsonLineItem.UniversalID, ProductName = jsonLineItem.UniversalName, Currency = jsonLineItem.Currency, ItemCategory = jsonLineItem.ItemCategory, MsrPrice = jsonLineItem.MsrPrice, PricePaid = jsonLineItem.PricePaid, Quantity = jsonLineItem.Quantity, UnitOfMeasure = jsonLineItem.UnitOfMeasure };

            var otherInfo = OtherInfoFromJSON(args[2]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitDeleteFromCart(cartID, lineItem, otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitUpgrade(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var otherInfo = OtherInfoFromJSON(args[0]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitUpgrade(otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitTrialUpgrade(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var otherInfo = OtherInfoFromJSON(args[0]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitTrialUpgrade(otherInfo);

                DispathOkResult();
            });
        }

        public void emitter_emitScreenView(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 7)
            {
                DispathWrongParametersResult();
                return;
            }

            string documentTitle = args[0];
            string contentDescription = args[1];
            string documentLocationUrl = args[2];
            string documentHostName = args[3];
            string documentPath = args[4];

            var otherInfo = OtherInfoFromJSON(args[5]);

            ExecuteAndWait(() =>
            {
                Emitter.Instance.EmitScreenView(documentTitle, contentDescription, documentLocationUrl, documentHostName, documentPath, otherInfo);

                DispathOkResult();
            });
        }


        public void push_isStarted(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(Push.Instance.Started);
            });
        }

        public void push_isPushEnabled(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(Push.Instance.Enabled);
            });
        }

        public void push_setPushEnabled(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            bool newSetEnabled = false;
            if (!bool.TryParse(args[0], out newSetEnabled))
            {
                DispathWrongParametersResult();
                return;
            }

            ExecuteAndWait(() =>
            {
                Push.Instance.Enabled = newSetEnabled;

                DispathOkResult();
            });
        }

        public void push_getDeviceToken(string json)
        {
            ExecuteAndWait(() =>
            {
                if (Push.Instance.ToastChannel == null)
                {
                    DispathOkResult("");
                    return;
                }

                DispathOkResult(Push.Instance.ToastChannel.ToString());
            });
        }

        public void push_getTags(string json)
        {
            ExecuteAndWait(() =>
            {
                DispathOkResult(Push.Instance.Tags.AllTags);
            });
        }

        public void push_addTag(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var newTag = args[0];

            ExecuteAndWait(() =>
            {
                Push.Instance.Tags.AddTag(newTag);

                DispathOkResult();
            });
        }

        public void push_removeTag(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var existTag = args[0];

            ExecuteAndWait(() =>
            {
                Push.Instance.Tags.DeleteTag(existTag);

                DispathOkResult();
            });
        }

        public void push_setTags(string json)
        {
            string[] args = JSON.JsonHelper.Deserialize<string[]>(json);

            if (args.Length < 2)
            {
                DispathWrongParametersResult();
                return;
            }

            var newTags = JSON.JsonHelper.Deserialize<string[]>(args[0]);

            ExecuteAndWait(() =>
            {
                Push.Instance.Tags.DeleteTags(Push.Instance.Tags.AllTags);
                Push.Instance.Tags.AddTags(newTags);

                DispathOkResult();
            });
        }

        private Dictionary<string, object> OtherInfoFromJSON(string json)
        {
            Dictionary<string, object> obj = null;

            try
            {
                obj = JsonConvert.DeserializeObject<Dictionary<string, object>>(json);
            }
            catch(Exception)
            {
            }

            return obj;
        }

        private string _commandCallbackId = null;
        private void ExecuteAndWait(Action a)
        {
            var commandCallbackId = CurrentCommandCallbackId;
            var e = new ManualResetEvent(false);

            System.Windows.Deployment.Current.Dispatcher.BeginInvoke(() => {
                _commandCallbackId = commandCallbackId;
                a();

                e.Set();
            });

            e.WaitOne();
        }

        private void DispathWrongParametersResult()
        {
            DispatchCommandResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION), _commandCallbackId);
        }

        private void DispathOkResult()
        {
            DispatchCommandResult(new PluginResult(PluginResult.Status.OK), _commandCallbackId);
        }

        private void DispathOkResult(object objValue)
        {
            DispatchCommandResult(new PluginResult(PluginResult.Status.OK, objValue), _commandCallbackId);
        }
        
        [DataContract]
        public class Toast
        {
            [DataMember(Name = "text1", IsRequired = false)]
            public string Title { get; set; }

            [DataMember(Name = "text2", IsRequired = false)]
            public string Subtitle { get; set; }

            [DataMember(Name = "param", IsRequired = false)]
            public string Param { get; set; }
        }
          
        [DataContract]
        public class Location
        {
            [DataMember(Name = "latitude", IsRequired = true)]
            public double Latitude { get; set; }

            [DataMember(Name = "longitude", IsRequired = true)]
            public double Longitude { get; set; }
        }

        [DataContract]
        public class UserInfo
        {
            [DataMember(Name = "userID", IsRequired = false)]
            public string UserID { get; set; }

            [DataMember(Name = "userName", IsRequired = false)]
            public string UserName { get; set; }

            [DataMember(Name = "email", IsRequired = false)]
            public string Email { get; set; }

            [DataMember(Name = "firstName", IsRequired = false)]
            public string FirstName { get; set; }

            [DataMember(Name = "lastName", IsRequired = false)]
            public string LastName { get; set; }

            [DataMember(Name = "streetAddress", IsRequired = false)]
            public string StreetAddress { get; set; }

            [DataMember(Name = "phone", IsRequired = false)]
            public string Phone { get; set; }

            [DataMember(Name = "city", IsRequired = false)]
            public string City { get; set; }

            [DataMember(Name = "state", IsRequired = false)]
            public string State { get; set; }

            [DataMember(Name = "zip", IsRequired = false)]
            public string Zip { get; set; }
        }

        [DataContract]
        public class SocialPermission
        {
            [DataMember(Name = "name", IsRequired = true)]
            public string Name { get; set; }

            [DataMember(Name = "granted", IsRequired = false)]
            public bool Granted { get; set; }
        }

        [DataContract]
        public sealed class LineItem
        {
            [DataMember(Name = "currency", IsRequired = false)]
            public string Currency { get; set; }

            [DataMember(Name = "itemCategory", IsRequired = false)]
            public string ItemCategory { get; set; }

            [DataMember(Name = "msrPrice", IsRequired = false)]
            public double MsrPrice { get; set; }

            [DataMember(Name = "pricePaid", IsRequired = false)]
            public double PricePaid { get; set; }

            [DataMember(Name = "productID", IsRequired = false)]
            public string ProductID { get; set; }

            [DataMember(Name = "productName", IsRequired = false)]
            public string ProductName { get; set; }

            [DataMember(Name = "itemID", IsRequired = false)]
            public string ItemID { get; set; }

            [DataMember(Name = "itemName", IsRequired = false)]
            public string ItemName { get; set; }

            [DataMember(Name = "quantity", IsRequired = false)]
            public int Quantity { get; set; }

            [DataMember(Name = "unitOfMeasure", IsRequired = false)]
            public string UnitOfMeasure { get; set; }

            public string UniversalID
            {
                get
                {
                    if (ProductID != null)
                        return ProductID;

                    return ItemID;
                }
            }

            public string UniversalName
            {
                get
                {
                    if (ProductName != null)
                        return ProductName;

                    return ItemName;
                }
            }
        }
    }
}