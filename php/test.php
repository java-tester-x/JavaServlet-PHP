<?php

session_start();
$_SESSION["session_id"] = session_id();

require_once('library.php');

?>

<html>
    <head>
        <script type="text/javascript">
            (function poll() {
               setTimeout(function() {
                   var r = new XMLHttpRequest(); 
                   r.open("POST", "polling.php", true);
                   r.setRequestHeader('Content-type','application/x-www-form-urlencoded');
                   r.setRequestHeader("X_REQUESTED_WITH",'XMLHttpRequest');
                   r.onreadystatechange = function () {
                       if (r.readyState != 4 || r.status != 200) return;
                       console.log(r.responseText);
                       poll();
                   };
                   r.send("tick=tack");
                }, 240000);
            })();
        </script>
    </head>
    <body>
        <p>Query</p>
        <form action="test.php" method="post">
            <lable for="query">SQL</lable>
            <textarea name="query" id="query" cols="80" rows="10"><?=@$_POST['query'];?></textarea>
            <input type="submit" name="submit_query" value="Run">
        </form>
        
        <?php if (!empty($_POST)): ?>
            <hr/
            <p>Result</p>
            <div>
                <?php
                    $page = Page::getInstance();
                    echo $page->doPost();
                ?>
            </div>
        <?php endif;?>
    </body>
</html>
