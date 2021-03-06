import {Plugin, IUserProperties} from "app/models";
import {CacheService} from "app/services";
import {PluginsService} from "../../ng2/services/plugins.service";


interface IPluginsTabViewModelScope extends ng.IScope {
    plugin: Plugin
    user: IUserProperties;
    version: string;
    queryParams: Object;
    isLoading: boolean;

    onLoadingDone(plugin: Plugin): void;
}

export class PluginsTabViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        'Sdc.Services.CacheService',
        'PluginsService'
    ];

    constructor(private $scope:IPluginsTabViewModelScope,
                private $stateParams:any,
                private cacheService:CacheService,
                private pluginsService:PluginsService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.plugin = this.pluginsService.getPluginByStateUrl(this.$stateParams.path);
        this.$scope.version = this.cacheService.get('version');
        this.$scope.user = this.cacheService.get('user');

        // Don't show the loader if the plugin isn't online
        if (this.$scope.plugin.isOnline) {
            this.$scope.isLoading = true;
        }

        this.$scope.queryParams = {
            userId: this.$scope.user.userId,
            userRole: this.$scope.user.role,
            displayType: "tab",
            parentUrl: window.location.origin,
            eventsClientId: this.$scope.plugin.pluginId
        };

        this.$scope.onLoadingDone = (plugin: Plugin) => {
            if (plugin.pluginId == this.$scope.plugin.pluginId) {
                this.$scope.isLoading = false;
            }
        };
    }
}
