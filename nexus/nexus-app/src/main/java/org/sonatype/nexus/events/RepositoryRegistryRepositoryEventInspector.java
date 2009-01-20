/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventUpdate;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;

/**
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryRegistryRepositoryEvent" )
public class RepositoryRegistryRepositoryEventInspector
    extends AbstractFeedRecorderEventInspector
{
    @Requirement
    private IndexerManager indexerManager;

    protected IndexerManager getIndexerManager()
    {
        return indexerManager;
    }

    public boolean accepts( AbstractEvent evt )
    {
        if ( evt instanceof RepositoryRegistryRepositoryEvent )
        {
            return true;
        }
        return false;
    }

    public void inspect( AbstractEvent evt )
    {
        inspectForNexus( evt );

        inspectForIndexerManager( evt );
    }

    private void inspectForNexus( AbstractEvent evt )
    {
        RepositoryRegistryRepositoryEvent revt = (RepositoryRegistryRepositoryEvent) evt;

        Repository repository = revt.getRepository();

        StringBuffer sb = new StringBuffer();

        if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            sb.append( " repository group " );
        }
        else
        {
            sb.append( " repository " );
        }

        sb.append( revt.getRepository().getName() );

        sb.append( " (ID=" );

        sb.append( revt.getRepository().getId() );

        sb.append( ") " );

        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            sb.append( " as proxy repository for URL " );

            sb.append( revt.getRepository().adaptToFacet( ProxyRepository.class ).getRemoteUrl() );
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class ) )
        {
            sb.append( " as hosted repository" );
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            sb.append( " as " );

            sb.append( revt.getRepository().getClass().getName() );

            sb.append( " virtual repository for " );

            sb.append( revt.getRepository().adaptToFacet( ShadowRepository.class ).getMasterRepository().getName() );

            sb.append( " (ID=" );

            sb.append( revt.getRepository().adaptToFacet( ShadowRepository.class ).getMasterRepository().getId() );

            sb.append( ") " );
        }

        sb.append( "." );

        if ( revt instanceof RepositoryRegistryEventAdd )
        {
            sb.insert( 0, "Registered" );
        }
        else if ( revt instanceof RepositoryRegistryEventRemove )
        {
            sb.insert( 0, "Unregistered" );
        }
        else if ( revt instanceof RepositoryRegistryEventUpdate )
        {
            sb.insert( 0, "Updated" );
        }

        getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, sb.toString() );

    }

    private void inspectForIndexerManager( AbstractEvent evt )
    {
        try
        {
            Repository repository = ( (RepositoryRegistryRepositoryEvent) evt ).getRepository();

            // we are handling repo events, like addition and removal
            if ( RepositoryRegistryEventAdd.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().addRepositoryIndexContext( repository.getId() );
            }
            else if ( RepositoryRegistryEventRemove.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().removeRepositoryIndexContext( repository.getId(), false );
            }
            else if ( RepositoryRegistryEventUpdate.class.isAssignableFrom( evt.getClass() ) )
            {
                getIndexerManager().updateRepositoryIndexContext( repository.getId() );
            }
        }
        catch ( Exception e )
        {
            getLogger().error( "Could not maintain indexing contexts!", e );
        }
    }

}
