<?php

namespace Frosh\MailArchive\Content\MailArchive;

use Shopware\Core\Checkout\Customer\CustomerDefinition;
use Shopware\Core\Framework\DataAbstractionLayer\EntityDefinition;
use Shopware\Core\Framework\DataAbstractionLayer\Field\FkField;
use Shopware\Core\Framework\DataAbstractionLayer\Field\Flag\AllowHtml;
use Shopware\Core\Framework\DataAbstractionLayer\Field\Flag\PrimaryKey;
use Shopware\Core\Framework\DataAbstractionLayer\Field\Flag\Required;
use Shopware\Core\Framework\DataAbstractionLayer\Field\Flag\SearchRanking;
use Shopware\Core\Framework\DataAbstractionLayer\Field\IdField;
use Shopware\Core\Framework\DataAbstractionLayer\Field\JsonField;
use Shopware\Core\Framework\DataAbstractionLayer\Field\LongTextField;
use Shopware\Core\Framework\DataAbstractionLayer\Field\ManyToOneAssociationField;
use Shopware\Core\Framework\DataAbstractionLayer\Field\StringField;
use Shopware\Core\Framework\DataAbstractionLayer\FieldCollection;
use Shopware\Core\System\SalesChannel\SalesChannelDefinition;

class MailArchiveDefinition extends EntityDefinition
{
    public const ENTITY_NAME = 'frosh_mail_archive';

    public function getEntityName(): string
    {
        return self::ENTITY_NAME;
    }

    public function getEntityClass(): string
    {
        return MailArchiveEntity::class;
    }

    public function getCollectionClass(): string
    {
        return MailArchiveCollection::class;
    }

    protected function defineFields(): FieldCollection
    {
        return new FieldCollection([
            (new IdField('id', 'id'))->addFlags(new PrimaryKey(), new Required()),
            (new JsonField('sender', 'sender'))->addFlags(new Required()),
            (new JsonField('receiver', 'receiver'))->addFlags(new Required())->addFlags(new SearchRanking(SearchRanking::HIGH_SEARCH_RANKING)),
            (new StringField('subject', 'subject'))->addFlags(new Required())->addFlags(new SearchRanking(SearchRanking::HIGH_SEARCH_RANKING)),
            (new LongTextField('plainText', 'plainText'))->addFlags(new AllowHtml()),
            (new LongTextField('htmlText', 'htmlText'))->addFlags(new AllowHtml(), new SearchRanking(SearchRanking::LOW_SEARCH_RANKING)),
            (new LongTextField('eml', 'eml'))->addFlags(new AllowHtml()),

            new FkField('salesChannelId', 'salesChannelId', SalesChannelDefinition::class),
            new ManyToOneAssociationField('salesChannel', 'salesChannelId', SalesChannelDefinition::class, 'id', true),

            new FkField('customerId', 'customerId', CustomerDefinition::class),
            new ManyToOneAssociationField('customer', 'customerId', CustomerDefinition::class, 'id', true),
        ]);
    }
}