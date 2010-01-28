/**
 *
 * Copyright (C) 2009 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package org.jclouds.compute.domain.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.InetAddress;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.jclouds.compute.domain.ComputeType;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.NodeState;
import org.jclouds.domain.Credentials;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Adrian Cole
 * @author Ivan Meredith
 */
public class NodeMetadataImpl extends ComputeMetadataImpl implements NodeMetadata {
   /** The serialVersionUID */
   private static final long serialVersionUID = 7924307572338157887L;

   private final NodeState state;
   private final Set<InetAddress> publicAddresses = Sets.newLinkedHashSet();
   private final Set<InetAddress> privateAddresses = Sets.newLinkedHashSet();
   private final Map<String, String> extra = Maps.newLinkedHashMap();
   private final Credentials credentials;
   private final String tag;

   public NodeMetadataImpl(String id, String name, String locationId, URI uri,
            Map<String, String> userMetadata, String tag, NodeState state,
            Iterable<InetAddress> publicAddresses, Iterable<InetAddress> privateAddresses,
            Map<String, String> extra, @Nullable Credentials credentials) {
      super(ComputeType.NODE, id, name, locationId, uri, userMetadata);
      this.tag = checkNotNull(tag, "tag");
      this.state = checkNotNull(state, "state");
      Iterables.addAll(this.publicAddresses, checkNotNull(publicAddresses, "publicAddresses"));
      Iterables.addAll(this.privateAddresses, checkNotNull(privateAddresses, "privateAddresses"));
      this.extra.putAll(checkNotNull(extra, "extra"));
      this.credentials = credentials;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getTag() {
      return tag;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Credentials getCredentials() {
      return credentials;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<InetAddress> getPublicAddresses() {
      return publicAddresses;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<InetAddress> getPrivateAddresses() {
      return privateAddresses;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public NodeState getState() {
      return state;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<String, String> getExtra() {
      return extra;
   }

   @Override
   public String toString() {
      return "[id=" + getId() + ", tag=" + getTag() + ", name=" + getName() + ", location="
               + getLocationId() + ", uri=" + getUri() + ", userMetadata=" + getUserMetadata()
               + ", state=" + getState() + ", privateAddresses=" + privateAddresses
               + ", publicAddresses=" + publicAddresses + "]";
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((credentials == null) ? 0 : credentials.hashCode());
      result = prime * result + ((extra == null) ? 0 : extra.hashCode());
      result = prime * result + ((privateAddresses == null) ? 0 : privateAddresses.hashCode());
      result = prime * result + ((publicAddresses == null) ? 0 : publicAddresses.hashCode());
      result = prime * result + ((state == null) ? 0 : state.hashCode());
      result = prime * result + ((tag == null) ? 0 : tag.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      NodeMetadataImpl other = (NodeMetadataImpl) obj;
      if (credentials == null) {
         if (other.credentials != null)
            return false;
      } else if (!credentials.equals(other.credentials))
         return false;
      if (extra == null) {
         if (other.extra != null)
            return false;
      } else if (!extra.equals(other.extra))
         return false;
      if (privateAddresses == null) {
         if (other.privateAddresses != null)
            return false;
      } else if (!privateAddresses.equals(other.privateAddresses))
         return false;
      if (publicAddresses == null) {
         if (other.publicAddresses != null)
            return false;
      } else if (!publicAddresses.equals(other.publicAddresses))
         return false;
      if (state == null) {
         if (other.state != null)
            return false;
      } else if (!state.equals(other.state))
         return false;
      if (tag == null) {
         if (other.tag != null)
            return false;
      } else if (!tag.equals(other.tag))
         return false;
      return true;
   }

}