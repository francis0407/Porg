<?php
require "config.php";

// readFile.php
$result = array('status' => 1,'message'=>'\n');
// check inputs
if( !(isset($_POST['url']) && isset($_POST['slice'])) ){
    $result['status'] = 0;
    $result['message'] = "Required parameter missing";
    echo json_encode($result);
    exit(1);
}
else{
    $url = $_POST['url'];

    $index_url = $_POST['url'].'_'.'index';
    
    $file = fopen($url,"r") or die(Message("Can't open file ".$_POST['url'],$result));
    $index = fopen($index_url,"r") or die(Message("Can't open index of ".$_POST['url'],$result));

    $index_data = json_decode(fread($index,filesize($index_url)));
    $start = $index_data[(int)($_POST['slice'])];
    $len = $index_data[((int)$_POST['slice'])+1] - $start;
    

    !fseek($file,$start) or die(Message("Can't seek index ".$start,$result));

    $data = fread($file,$len < 0 ? filesize($url): $len);
    if($data == false)
        $data = "";
    $result['status'] = 1;
    $result['message'] = "succeed";
    $result['len'] = strlen($data);
    $result['data'] = $data;

    echo json_encode($result);

}


function Message($m,$r){
    $r['status'] = 0;
    $r['message'] = $m;
    return json_encode($r);
}

?>