<?php
require "config.php";

// makeDir.php
$result = array('status' => 1,'message'=>'\n');
// check inputs
if(!isset($_POST['url']) || !isset($_POST['map_size'])){
    $result['status'] = 0;
    $result['message'] = "Required parameter missing";
    echo json_encode($result);
    exit(1);
}
else{
    $map_size = $_POST['map_size'];
    $url = $_POST['url'];
    $size = filesize($url);
    $index_url = $url.'_index';

    !file_exists($index_url) or die(Message("Index file exists.",$result));
    $file = fopen($url,"r") or die(Message("Can't open file ".$_POST['url'],$result));
    $index_file = fopen($index_url,"w") or die(Message("Can't create index file",$result));      
    
    $count = 0;
    $index = array();
    while($size > $count){
        array_push($index,$count);
        !fseek($file,$count) or die(Message("Can't seek index ".$count,$result));
        $temp = fread($file,$map_size);
        $pos = strrpos($temp,"\n");
        $count = $count + $pos + 1;
    }
    array_push($index,$count);
    fwrite($index_file,json_encode($index));
    $result['map_num'] = count($index) - 1;
    $result['status'] = 1;
    echo json_encode($result);
}