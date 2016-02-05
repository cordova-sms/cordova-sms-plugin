using System;
using Microsoft.Phone.Tasks;
using WPCordovaClassLib.Cordova;
using WPCordovaClassLib.Cordova.Commands;
using WPCordovaClassLib.Cordova.JSON;

namespace WPCordovaClassLib.Cordova.Commands
{
  public class Sms : BaseCommand
  {
    public void send(string options)
    {
      string[] optValues = JsonHelper.Deserialize<string[]>(options);
      string[] numValues = JsonHelper.Deserialize<string[]>(optValues[0]);
      String number = String.Join(",", numValues);
      String message = optValues[1];

      SmsComposeTask task = new SmsComposeTask();
      task.Body = message;
      task.To = number;
      task.Show();
    }
  }
}
