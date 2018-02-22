# PHAT Example Variation Activity [03]
A user presents an episode of tremors in the hands and neck.
While the episode occurs, the gesture of opening a door is assigned. The gesture is deteriorated to a different degree with both hands.

While performing the gesture, two accelerometry sensors positioned in both hands are read.
<table>
<tr>
    <td>
        <img height="80" width="80" src="https://github.com/mfcardenas/phat_example_monitoring_03/blob/master/img/in_progress.png" title="The example is under construction"/>
    </td>
    <td>  
To run the demo

```
mvn clean compile
mvn exec:java -Dexec.mainClass=phat.ActvityMonitoringDemo
```
In case of running into memory problems
```
export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=128m"
```
And then run the previous command
    </td>
    <td>
        <img src="https://github.com/mfcardenas/phat_example_monitoring_03/blob/master/img/img_older_people_home.png" />
    </td>
</tr>
</table>
