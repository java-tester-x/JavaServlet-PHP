<?php

session_start();

require_once('library.php');

$security = Security::getInstance();
if (! $security->is_ajax_request()) {
    exit;
}

$page = Page::getInstance();
echo $page->doPoll();
?>