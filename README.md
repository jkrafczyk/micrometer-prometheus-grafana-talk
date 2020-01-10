# Spring / Micrometer / Prometheus / Grafana Demosetup

## Starten des gesamten toolstacks

`docker-compose up` ausführen.

Das bauen des images für die Beispielanwendung kann ein paar Minuten dauern.

Wenn alles gestartet ist, sind folgende URLs interessant:  

| URL | Beschreibung |
| --- | ------------ | 
| http://localhost:8080/ | Beispielanwendung. Die Startseite sollte eine 404-Fehlermeldung ausliefern. |
| http://localhost:3000/ | Grafana |
| http://localhost:9090/ | Prometheus|

Um Daten für eventuelle Grafana-Dashboards bereitzustellen, können die Shelllscripte `do-requests.sh` oder `largeleak.sh` ausgeführt werden.  
Ersteres führt in einer Schleife immer wieder Anfragen gegen sämtliche Controller der Beispielanwendung durch. Letzteres führt einmalig mehrere Requests durch, die memory-leaks auslösen.


## HTTP Endpoints in Beispielanwendung

| Methode | Pfad | Beschreibung |
|-------- | ---- | ----------- |
| GET | /actuator/prometheus | Proemtheus-Scraping-Ziel. Liefert die aktuellen Metriken im Prometheus format zurück. |
| POST | /leak | Controller der bei jedem Request ca. 5MB Speicher anfordert und nicht wieder freigibt. Memory leak. |
| GET | /slow | Wartet zwischen 500 und 3000 millisekunden bevor eine Antwort zurück gegeben wird. |
| GET | /greet | Liefert eine allgemeine Begrüßung 'Hello, World' zurück. |
| GET | /greet?name=Bob | Liefert eine Begrüßung für den übergebenen Namen zurück, in diesem Fall 'Hello, Bob'. |

## Beispielanfragen mit curl

* `curl -v http://localhost:8080/actuator/prometheus`
* `curl -v -XPOST http://localhost:8080/leak`
* `curl -v http://localhost:8080/slow`
* `curl -v http://localhost:8080/greet`
* `curl -v http://localhost:8080/greet\?name=Bob`

## Verzeichnisstruktur

| | |
|----|----|
| app | Quellcode der Beispielanwendung, inkl. Dockerfile |
| volumes/config | Konfigurationsdateien für Prometheus und Grafana |
| volumes/config/grafana/provisioning | Grafana-Konfiguration für Prometheus-Zugang und Beispieldashboard |  
| volumes/storage | Volumes für Prometheus und Grafana Datenbanken |

# Vortragsinhalte

## Demo-Applikation

* Spring-Boot Webapplikation in Kotlin
* Zielplatform Java 11
* Wenige einfache Controller:
    * GreetController - Liefert "Hello, World" oder "Hello, [name]" Texte aus. Sammelt Statistiken über die Länge der übergebenen Namen.
    * SlowController - Liefert Antworten erst nach kurzer Verzögerung aus. Sammelt Statistiken über die Verzögerungsdauern.
    * LeakController - Hat ein Memory-Leak welches bei jedem Request ausgelöst wird.

## Micrometer

* Bibliothek zum Sammeln und Publizieren von Metriken aus Appliatkionen heraus.
* Abstrahiert verschiedene Monitoringsysteme / Protokolle / Datenbanken, z.B.:
    * Prometheus
    * Influx
    * Stackdriver
    * JMX
* Micrometer ist für Metriken, was SLF4J für Logging ist
* Integration in Spring-Boot-Applikation:
    * Abhängigkeiten `org.springframework.boot:spring-boot-starter-actuator` und die gewünschte Micrometer-Implementierung hinzufügen (z.B. `io.micrometer:micrometer-registry-prometheus`)
    * Konfiguration in application.properties. Im Prometheus-Fall: Actuator-Endpunkte und Metrikexport nach Prometheus aktivieren
* Verschiedene JVM-Metriken und Daten von Spring werden automatisch verfügbar gemacht:
    * Speicherbedarf
    * GC-Statistiken
    * Aktive Threads
    * Falls über Spring-Boot konfiguriert: Daten über JDBC Connection-Pools
    * Timingdaten für alle RequestMappings
    * u.v.m.
* Aufwand zum bereitstellen eigener Metriken: Wenige Zeilen Code
    * MeterRegistry injecten lassen
    * Eigene Metrik in MeterRegistry anmelden (z.B. per `meterRegistry.counter()` oder `meterRegistry.timer()`)
    * Daten nach Bedarf per `.record()` auf das `Meter`-Objekt aufzeichnen
    * Beispielcode in GreetController und SlowController 

## Prometheus

* Monitoring-Tool mit integrierter Time-Series-Datenbank
* Sammelt Daten von Applikationen durch regelmäßiges Polling ein
* Anwendung muss Metriken als Antwort auf per HTTP-Request in Textformat bereitstellen
* Beispiel für das Format: Bei laufender Demo-Applikation unter http://localhost:8080/actuator/prometheus ansehen!
* In diesem Repository: URL zur Applikation in Prometheus-Konfigdatei hinterlegt (volumes/config/prometheus/prometheus.yaml)
* Im Produktivbetrieb besser: Service-Discovery, z.B. über Kubernetes-API, Docker, Consul, oder andere Mechanismen (https://prometheus.io/docs/prometheus/latest/configuration/configuration/)
* Bietet eine einfache Oberfläche z.B. Ausführen von Queries an (http://localhost:9090/graph)

## Grafana

* UI um Datenbankanfragen zu stellen und die Ergebnisse davon zu plotten
* Plugin-Mechanismus für Datenbanken, Plots und Alerting
* Kann Prometheus, InfluxDB, PostgreSQL, ElasticSearch, u.v.m. Anfragen und Benachrichtugungen u.A. nach Slack, Hangouts, PagerDuty versenden.
* Von der Community erstellte Dashboards können unter https://grafana.com/grafana/dashboards gesucht und in die eigene Installation importiert werden.
* Zum Import: In der Navigation Dashboards -> Manage öffnen, dort den Import-Button verwenden.
* Ein brauchbares Micrometer/Prometheus-Dashboard kann mit der ID 4701 importiert werden. (https://grafana.com/grafana/dashboards/4701)
* Empfehlungen zum Kennenlernen der Prometheus-Querysprache: 
    * In existierendem Dashboard bestehende Graphen editieren um die verwendeten Queries zu sehen
    * In der Explore-Ansicht von Grafana herumspielen. Auto-Completion ist vorhanden! 

## Sonstiges
* Host-Monitoring mit Prometheus z.B. mit node-exporter möglich. Exporter für verschiedene andere Anwendungen existieren. (https://prometheus.io/docs/instrumenting/exporters/)
* Alerts können entweder in Grafana erstellt und ausgeführt werden, oder mit dem separaten prometheus-alertmanager (https://prometheus.io/docs/alerting/alertmanager/)
* Prometheus ist selber nicht unbedingt für langfristige Storage von Daten ausgelegt. Standard-retention-Zeit: wenige Tage
* Lösung dafür: Prometheus kann Daten in separate Datenbank (z.B. PostgreSQL mit Timescale extension, oder InfluxDB) exportieren (https://prometheus.io/docs/prometheus/latest/configuration/configuration/#remote_write)
