(ns puppetlabs.puppetdb.integration.reports.transaction-uuid
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [puppetlabs.puppetdb.integration.fixtures :as int]
            [puppetlabs.puppetdb.testutils.services :as svc-utils]
            [puppetlabs.puppetdb.cheshire :as json]))

(deftest ^:integration transaction-uuid-populated
  (with-open [pg (int/setup-postgres)
              pdb (int/run-puppetdb pg {})
              ps (int/run-puppet-server-as "something_silly" [pdb] {})]
    (testing "Initial agent run, to populate puppetdb with data to query"
      (int/run-puppet-as "my_agent" ps pdb
                         (str "notify { 'hi':"
                              "  message => 'Hi foo' "
                              "}")))
    (let [[report] (int/pql-query pdb "reports { certname = 'my_agent' }")
          [catalog] (int/pql-query pdb "catalogs { certname = 'my_agent' }")]
      (is (:transaction_uuid report))
      (is (:transaction_uuid catalog))
      (is (= (:transaction_uuid report)
             (:transaction_uuid catalog))))))
