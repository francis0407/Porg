<?php

/**
 * declare for debug
 * checkout workerman.log for more information
 */
//declare(ticks=1);

/**
 * Web Server of Prog
 * Focus on onMessage onClose 
 */
use \GatewayWorker\Lib\Gateway;

class Events
{

   /**
    * Triggered when recieve from Browser or JobTracker
    * @param int $client_id
    * @param mixed $message
    */
   public static function onMessage($client_id, $message)
   {
        // Debug
        echo "client:{$_SERVER['REMOTE_ADDR']}:{$_SERVER['REMOTE_PORT']} gateway:{$_SERVER['GATEWAY_ADDR']}:{$_SERVER['GATEWAY_PORT']}  client_id:$client_id session:".json_encode($_SESSION)." onMessage:".$message."\n";
        
        // Parse json messages
        $message_data = json_decode($message, true);
        if(!$message_data)
        {
            return ;
        }
        
        // Check status
        if ($message_data['status'] == 0)
        {
            echo "status 0: $message_data[message]";
            return ;
        }

        // Actions
        switch($message_data['action'])
        {
            // Recieve HeartBeat
            case 'pong':
                return;

            // Connected with JobTracker, join group tracker(only one connected)
            case 'tracker':
                Gateway::joinGroup($client_id, 'tracker');
                return ;
            
            // Browser finished I/O speed test
            case 'connect':
                // exception
                if(!isset($message_data['data']['speed']))
                {
                    throw new \Exception("\$data['speed'] not set. client_ip:{$_SERVER['REMOTE_ADDR']}");
                }
                $speed = $message_data['data']['speed'];
                $data = array('uid'=>$client_id,'speed'=>$speed);
                // message format: {status:1, message:'', action:connect, data:xx}
                $new_message = array('status'=>1,'message'=> '', 'action'=>'connect', 'data'=>$data);
                // Seed speed to JobTracker
                Gateway::sendToGroup('tracker', json_encode($new_message));
                return ;
            
            // Browser finished job
            case 'finish':
                // Add uid information
                $data = $message_data['data'];
                $data['uid'] = $client_id;
                // message format: {status:1, message:'', action:finish, data:xx}
                $new_message = array('status'=>1,'message'=>'', 'action'=>'finish', 'data'=>$data);
                // Seed finish information to JobTracker
                Gateway::sendToGroup('tracker', json_encode($new_message));
                return ;

            // error information
            case 'error':
                // Add uid information
                $data = $message_data['data'];
                // exception
                /*if(!isset($data['uid']))
                {
                    throw new \Exception("\data['uid'] not set. client_ip:{$_SERVER['REMOTE_ADDR']}");
                }*/
                $data['uid'] = $client_id;
                // message format: {status:1, message:'', action:error, data:xx}
                $new_message = array('status'=>1,'message'=>'', 'action'=>'error', 'data'=>$data);
                // Seed finish information to JobTracker
                Gateway::sendToGroup('tracker', json_encode($new_message));
                return ;
            
            // Job recieve from JobTracker
            case 'task':
                $data = $message_data['data'];
                $uid = $data['uid'];
                unset($data['uid']);
                // message format: {status:1, message:'', action:task, data:xx}
                $new_message = array('status'=>1,'message'=>'', 'action'=>'task', 'data'=>$data);
                if(!Gateway::isOnline($uid))
                {
                    echo "uid:{$uid} was off-line \n";
                    return ;
                }
                Gateway::sendToClient($uid, json_encode($new_message));
                return ;
        }
   }
   
   /**
    * Triggered when browser is closed
    * @param integer $client_id 
    */
   public static function onClose($client_id)
   {
       // debug
       echo "client:{$_SERVER['REMOTE_ADDR']}:{$_SERVER['REMOTE_PORT']} gateway:{$_SERVER['GATEWAY_ADDR']}:{$_SERVER['GATEWAY_PORT']}  client_id:$client_id onClose:''\n";
       
       // inform JobTracker client was closed 
        $data['uid'] = $client_id;
        // message format: {status:1, message:'', action:disconnect, data:xx}
        $new_message = array('status'=>1,'message'=>'', 'action'=>'error', 'data'=>$data);
        // Seed finish information to JobTracker
        Gateway::sendToGroup('tracker', json_encode($new_message));
   }
  
}
