# Akqa Meeting Schedular Exercise
akqa-meeting-schedular-exercise

it is ab spring boot based, maven project
import it as maven project in ide

AkqaApplication is the main class

following parameters can be configured via application.properties

meeting.request.file.full.path=

meeting minimum slot time e.g. 60, 30, 15 or anything
meeting.interval=60


default format for office hour is 0900 1730 ,it can be changed
meeting.office.hours.pattern=^\\d{4} \\d{4}$

default meeting requester pattern is 2011-03-17 10:17:06 EMP001, it can also be changed
meeting.request.by.pattern=^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} EMP\\d{3}$

default meeting request pattern, it can also be changed
2011-03-21 09:00 2
meeting.request.for.pattern=^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2} \\d{1}$

should a meeting be scheduled beyond office hours
meeting.beyond.office.hours.allowed=false

alternatively meeting request file can be also provided via command line parameter

if no meeting request file is provided then it take an default meeting request file which has sample data described in the exercise (data-in.txt, in resources folder)

as of now schedule is printed on console though a file should have also been produced to be consumable by another program
