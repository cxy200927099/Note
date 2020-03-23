
## 服务部署常用脚本
在部署服务的时候，常常需要start，stop，ps等待，对于这些命令可能不同的服务会包含很多参数，每次都去敲这些命令总是很麻烦；因此常常
会用一个脚本封装起来，提供简单的命令，如下所示
```shell
#!/bin/bash
action=$1
DIR=$(cd `dirname $0`;cd ..;pwd)
# PROGRAM=`echo ${DIR}|awk -F/ '{print $NF}'`
PROGRAM='novel_reco_server'
cd $DIR

function Usage(){
    echo "xxx:[start|stop|ps|restart|monitor]"
}

function SetEnv(){
    source /home/root1/miniconda3/bin/activate python3-cxy
}

function Alarm()
{
     curl -s -H 'Content-Type: application/json' https://oapi.dingtalk.com/robot/send?access_token=dc671aee026853fdcb787e074cde54a04878afe968ead9ceb5ddb82bc8ebbf13 -d \
         "{\"msgtype\":\"text\",\"text\":{\"content\":\"alarm:${HOSTNAME}\\n$1\"}}"
}

function Start(){
    pid=`Ps`
    if [ -n "$pid" ]; then
        echo "$PROGRAM is running! [${pid[@]}]"
        exit 0
    else
        echo -en "Start $PROGRAM service:\t\t\t\t\t\n"
        # chmod +x $DIR/bin/$PROGRAM
        nohup python $DIR/novel_statistics/user_model_main.py --port=50051 --program=${PROGRAM} >> $DIR/log/nohup.out 2>&1 &
        [ -n "`Ps`" ] && echo "[ $PROGRAM start OK  ]" || echo "[ $PROGRAM start FAIL ]"
    fi
}

function Stop(){
    pid=`Ps`
    if [ -n "$pid" ]; then
        kill ${pid} 2> /dev/null
        echo -e "Stop $PROGRAM service:\t\t\t\t\t[  OK  ]"
    else
        echo "$PROGRAM is not running!"
    fi
}

function Ps()
{
    pid=$(ps -ef | grep -w "${PROGRAM}" | grep -v 'grep' | awk '{print $2}')
    echo $pid
}

function GetProcessCount(){
    p_cnt=$(ps -ef | grep -w "${PROGRAM}" | grep -v 'grep' | wc -l)
    echo $p_cnt
}

function CheckIfAlive(){
    #pid=`Ps`
    p_cnt=`GetProcessCount`
    alarm_msg=""
    if [ 2 == $p_cnt ]; then
        echo "$PROGRAM all process are running..."
        return
    elif [ 1 == $p_cnt ]; then
        alarm_msg="a $PROGRAM process was died"
    else
        alarm_msg="all $PROGRAM process was died"
    fi
    echo "$alarm_msg"
    Alarm "$alarm_msg，will restart after 5s"
    sleep 5
    SetEnv
    Stop
    Start
}

case $1 in
    start) SetEnv; Start;;
    stop) Stop;;
    restart) Stop; SetEnv; Start;;
    ps) Ps;;
    monitor) CheckIfAlive;;
    *) Usage;;
esac
```

上述脚本是一个通用的脚本，对于不同的服务，需要修改写里面具体的start，stop，ps等函数，
此脚本在最开始获取了命令执行的目录，后续执行命令都是绝对路径的方式，因此可以在任何一个目录执行






