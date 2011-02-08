;
;
; Copyright (C) 2010 Cloud Conscious, LLC. <info@cloudconscious.com>
;
; ====================================================================
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
; ====================================================================
;
(ns org.jclouds.ssh-test
  (:require
   [clojure.contrib.logging :as logging]
   [org.jclouds.modules :as modules])
  (:import
   org.jclouds.ssh.SshClient
   org.jclouds.compute.domain.ExecResponse
   org.jclouds.io.Payload
   org.jclouds.net.IPSocket))

(defn instantiate [impl-class & args]
  (let [constructor (first
                     (filter
                      (fn [c] (= (count args) (count (.getParameterTypes c))))
                      (.getDeclaredConstructors impl-class)))]
    (.newInstance impl-class (object-array args))))




;; define an instance or implementation of the following interfaces:

(defn maybe-invoke [f & args]
  (when f
    (apply f args)))

(defn default-exec
  "Default exec function - replies to ./runscript status by returning 1"
  [cmd]
  (merge
   {:exit 0 :err "stderr" :out "stdout"}
   (condp = cmd
       "./bootstrap status" {:exit 1 :out "[]"}
       {})))


(deftype NoOpClient
    [socket username password]
  SshClient
  (connect [this])
  (disconnect [this])
  (exec [this cmd]
        (logging/info (format "ssh cmd: %s" cmd))
        (let [response (default-exec cmd)]
          (ExecResponse. (:out response) (:err response) (:exit response))))
  (get [this path] )
  (^void put [this ^String path ^String content])
  (^void put [this ^String path ^org.jclouds.io.Payload content])
  (getUsername [this] username)
  (getHostAddress [this] (.getAddress socket)) )

(defn no-op-ssh-client
  [socket username password]
  (NoOpClient. socket username password))


(deftype SshClientFactory
    [factory-fn]
  org.jclouds.ssh.SshClient$Factory
  (^org.jclouds.ssh.SshClient
   create
   [_ ^IPSocket socket ^String username ^String password-or-key]
   (factory-fn socket username password-or-key))
  (^org.jclouds.ssh.SshClient
   create
   [_ ^IPSocket socket ^String username ^bytes password-or-key]
   (factory-fn socket username password-or-key)))

(deftype Module
    [factory binder]
  com.google.inject.Module
  (configure
   [this abinder]
   (reset! binder abinder)
   (.. @binder (bind org.jclouds.ssh.SshClient$Factory)
       (toInstance factory))))

(defn ssh-test-module
  "Create a module that specifies the factory for creating a test service"
  [factory]
  (let [binder (atom nil)]
    (Module. factory binder)))

(defn ssh-test-client
  "Create a module that can be passed to a compute-context, and which implements
an ssh client with the provided map of function implementations. Keys are
clojurefied versions of org.jclouds.ssh.SshClient's methods"
  [factory-fn]
  (ssh-test-module (SshClientFactory. factory-fn)))

