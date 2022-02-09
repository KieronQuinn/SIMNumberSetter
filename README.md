# SIM Number Setter

SIM Number Setter is a small Xposed module that invokes normally unused Android System code to set 
the "subscriber number" on the device's SIM card. This is the number displayed in the system 
settings, and used in apps such as Google Messages, as well as being available to third party apps 
with sufficient permissions.

The number is not always set by carriers (leaving "Unknown"), or can be lost or invalid after 
porting a number to a different SIM. 

This small app allows you to fix it, permanently, using a rooted device with Xposed.

**If you don't have a rooted device, or don't want to root your main device, you may wish to 
consider rooting an old device, and temporarily putting your SIM into that device, using this app,
and then putting it back in your main device - the fixed number will travel with it**

### Important Notice

Changing the SIM number is semi-permanent: the number change will survive reboots, uninstalling this
app, even switching the SIM to a different device, but can be changed again at any time using this 
app. 

SIM Number Setter does NOT:
- Change your actual phone number, no matter what you enter.
- Unblock a network blocked SIM.\n- Give you free data or calls.
- Change your IMEI.
  
SIM Number Setter uses the built-in Android methods to write data to the SIM. 
The app nor the developer are not responsible for issues with the process, including any damage to 
the SIM or network issues.

### Screenshots

[![Screenshots](https://i.imgur.com/UNKADmrl.png)](https://i.imgur.com/UNKADmr.png)

### Download

[Download from releases page](https://github.com/KieronQuinn/SIMNumberSetter/releases)