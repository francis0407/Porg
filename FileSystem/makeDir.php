<?php
require "config.php";

// makeDir.php
$result = array('status' => 1,'message'=>'\n');
// check inputs
if(!isset($_POST['job_name'])){
    $result['status'] = 0;
    $result['message'] = "Required parameter missing";
    echo json_encode($result);
    exit(1);
}
else{
    $time_stamp = (string)gettimeofday()['sec']; 
    $dir  = FS_DIR_PATH.'/'.$_POST['job_name'].'_'.$time_stamp;
    if(!mkdir($dir) || !mkdir($dir.'/map') || !mkdir($dir.'/result')){
        $result['status'] = 0;
        $result['message'] = "Can't create directory for job ".$_POST['job_name'];
        echo json_encode($result);
        exit(1);
    }
    $result['job_dir'] = $dir;
    echo json_encode($result);
}