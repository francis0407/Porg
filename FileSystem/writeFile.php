<?php
require "config.php";

// writeFile.php
$result = array('status' => 1,'message'=>'\n');

// check inputs
if( !(isset($_POST['type']) && isset($_POST['tid']) && isset($_POST['data']) ) ){
    $result['status'] = 0;
    $result['message'] = "Required parameter missing";
    echo json_encode($result);
    exit(1);
}
else{
    
    if($_POST['type'] == 'm'){
        // a temp map output with index
        isset($_POST['index']) or die(Message("Required 'index'.",$result));
        $url = JOB_DIR.'/'.'map_output_'.$_POST['tid'];
        $index_url = $url.'_index';
        !file_exists($url) or die(Message("File ".$url." exists.",$result,-1));
        !file_exists($index_url) or die(Message("Index file exists.",$result));

        $file = fopen($url,"w") or die(Message("Can't create file".$url,$result));
        $index_file = fopen($index_url,"w") or die(Message("Can't create index file",$result));      
        
        fwrite($file,$_POST['data']) or !len($_POST['data']) or die(Message("Can't write file $url",$result));
        fwrite($index_file,$_POST['index']) or !len($_POST['index']) or die(Message("Can't write index file $index_file",$result));

        $result['status'] = 1;
        $result['message'] = 'succeed';
        $result['url'] = 'map_output_'.$_POST['tid'];

        echo json_encode($result);
    }
    else if($_POST['type'] == 'r'){
        // a reduce result
        $url = JOB_DIR.'/'.'reduce_output_'.$_POST['tid'];
        !file_exists($url) or die(Message("File ".$url." exists.",$result,-1));
        $file = fopen($url,"w") or die(Message("Can't create file".$url,$result));
        fwrite($file,$_POST['data']) or !len($_POST['data']) or die(Message("Can't write file $url",$result));

        $result['status'] = 1;
        $result['message'] = 'succeed';
        $result['url'] = 'reduce_output_'.$_POST['tid'];

        echo json_encode($result);
    }
    else
        die(Message("Wrong type ".$_POST['type'],$result));
}

function Message($m,$r,$s = 0){ 
    $r['status'] = $s;
    $r['message'] = $m;
    return json_encode($r);
}

?>